package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.model.User;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model.BbCloudConnectLifecyclePayload;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;
import io.jenkins.blueocean.commons.JsonConverter;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.OrganizationRoute;
import io.jenkins.blueocean.rest.Utils;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineContainerImpl;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainSpecification;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
@ExportedBean
@Extension(ordinal = -9999)
public class BitbucketCloudConnect implements OrganizationRoute {
    public static final String BASE_URL_PROPERTY="bbcloud.baseurl";
    public static final String BB_CLOUD_CONNECT_CREDENTIAL_DOMAIN="bitbucket-cloud-connect";
    public static final String BB_CLOUD_CONNECT_CRED_PREFIX="bitbucket-cloud-connect-";
    public static final String BB_ADDON_KEY="jenkins-blueocean-addon";

    @CheckForNull
    @Override
    public String getUrlName() {
        return "bitbucket-connect";
    }

    private final String descriptor;
    private final BlueOrganization organization;

    public BitbucketCloudConnect() {
        this.organization = OrganizationFactory.getInstance().of(Jenkins.getInstance());
        if(this.organization == null){
            throw new ServiceException.UnexpectedErrorException("Failed to find Jenkins organization");
        }

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("io/jenkins/blueocean/blueocean_bitbucket_pipeline/cloud/atlassian-connect-template.json");
        if(inputStream == null){
            throw new ServiceException.UnexpectedErrorException("Failed to load Bitbucket Cloud Connect resource descriptor");
        }
        try {
            Map<String, Object> descMap = JsonConverter.om.readValue(new InputStreamReader(inputStream),
                    new TypeReference<Map<String, Object>>() {});
            descMap.put("key", "jenkins-blueocean-addon");
            descMap.put("baseUrl", getBaseUrl());
            this.descriptor = JsonConverter.toJson(descMap);
        }catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to parse Bitbucket Cloud Connect resource descriptor");
        }
    }

    /**
     * Initializes addon installation process.
     * @return HttpResponse with redirection
     */
    public HttpResponse doConnect(){
        User user = requireAuthentication();
        String baseUrl = getBaseUrl(false);
        final String redirectUrl = BitbucketCloudApi.initiateAddOnInstall(baseUrl+organization.getLink().rel(getUrlName()).rel("descriptor"), this.descriptor);
        return new HttpResponse(){
            @Override
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                //XXX: for some reason couldn't get redirection from backend to work with frontend code
                //     front-end does window.localtion.href=data.redirectUrl after receiving response
                rsp.setContentType(ContentType.APPLICATION_JSON.toString());
                rsp.getWriter().write(JsonConverter.toJson(ImmutableMap.of("redirectUrl", redirectUrl)));
            }
        };
    }

    /**
     * Finishes addon installation to a bitbukcet account
     * @param code Its a JWT token
     * @return redirection to create-pipeline page
     */
    public HttpResponse doDescriptor(@QueryParameter("code") String code){
        if(StringUtils.isBlank(code)){
            throw new ServiceException.PreconditionRequired("code query parameter must be present");
        }

        User user = requireAuthentication();
        final BbCloudConnectLifecyclePayload resp = BitbucketCloudApi.completeAddOnInstall(code, descriptor);
        final BbUser bbUser =  BitbucketCloudApi.getUserUsingJwt(BbConnectCredential.getJwtAuthHeader(resp.getSharedSecret(),
                resp.getClientKey()));

        if(bbUser == null){
            throw new ServiceException.PreconditionRequired("Failed to get user using obtained credentials");
        }

        String credentialId = String.format("%s%s",BB_CLOUD_CONNECT_CRED_PREFIX,resp.getOrganization().getKey());
        BbConnectCredential bbCredentials = CredentialsUtils.findCredential(credentialId,
                BbConnectCredential.class, new BlueOceanDomainRequirement());
        BbConnectCredential credential = new BbConnectCredential(
                CredentialsScope.USER,
                credentialId,
                "Bitbucket Cloud Connect credentials",
                bbUser.getSlug(),
                resp.getOrganization().getKey(),
                resp.getOrganization().getName(),
                resp.getSharedSecret(),
                resp.getClientKey());
        try {
            if (bbCredentials == null) {
                CredentialsUtils.createCredentialsInUserStore(
                        credential, user, BB_CLOUD_CONNECT_CREDENTIAL_DOMAIN,
                        ImmutableList.<DomainSpecification>of(new BlueOceanDomainSpecification()));
            }else{
                CredentialsUtils.updateCredentialsInUserStore(
                        bbCredentials, credential, user, BB_CLOUD_CONNECT_CREDENTIAL_DOMAIN,
                        ImmutableList.<DomainSpecification>of(new BlueOceanDomainSpecification()));
            }

            return new HttpResponse(){
                @Override
                public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                    rsp.sendRedirect(303,
                            String.format("%sblue/organizations/%s/create-pipeline?bitbucket",getBaseUrl(), organization.getName()));
                }
            };
        }catch (Exception e){
            throw new ServiceException.UnexpectedErrorException(e.getMessage());
        }
    }

    public MultiBranchPipelineContainerImpl doPipelines(){
        return null;
    }

    static String getBaseUrl(){
        return getBaseUrl(true);
    }

    private static String getBaseUrl(boolean ensureTrailingSlash){
        String baseUrl = System.getProperty(BASE_URL_PROPERTY);
        if(StringUtils.isBlank(baseUrl)) {
            String rootUrl = Jenkins.getInstance().getRootUrl();
            if (rootUrl == null) {
                throw new ServiceException.UnexpectedErrorException("Jenkins root URL is null");
            }
            baseUrl = rootUrl;
        }

        if(ensureTrailingSlash) {
            return Utils.ensureTrailingSlash(baseUrl);
        }else{
            return baseUrl.charAt(baseUrl.length()-1) == '/' ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        }
    }

    User requireAuthentication(){
        User authenticatedUser = User.current();
        if(authenticatedUser == null){
            throw new ServiceException.UnauthorizedException("User must be logged in to install Bitbucket Connect Add-On");
        }
        return authenticatedUser;
    }
}
