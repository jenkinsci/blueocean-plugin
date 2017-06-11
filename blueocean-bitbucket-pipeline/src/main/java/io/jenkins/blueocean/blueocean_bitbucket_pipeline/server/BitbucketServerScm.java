package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.User;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApiFactory;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbProject;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.stapler.JsonBody;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainSpecification;
import io.jenkins.blueocean.rest.impl.pipeline.scm.AbstractScm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFactory;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import io.jenkins.blueocean.rest.model.Container;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerScm extends AbstractScm {
    static final String DOMAIN_NAME="blueocean-bitbucket-server-domain";
    static final String ID = "bitbucket-server";

    private final Link self;
    //private final String apiUrl;

    public BitbucketServerScm(Reachable parent) {
        this.self = parent.getLink().rel(ID);
    }

    @Override
    public Object getState() {
        StaplerRequest request = Stapler.getCurrentRequest();
        Preconditions.checkNotNull(request, "Must be called in HTTP request context");

        String apiUrl = request.getParameter("apiUrl");

        if(StringUtils.isBlank(apiUrl)) {
            throw new ServiceException.BadRequestException("apiUrl is required parameter. apiUrl is API url of bitbucket server");
        }
        try {
            new URL(apiUrl);
        } catch (MalformedURLException e) {
            throw new ServiceException.BadRequestException("apiUrl is not a valid URL: "+e.getMessage(), e);
        }
        return super.getState();
    }

    @Override
    public Link getLink() {
        return self;
    }

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public String getUri() {
        return getApiUrlParameter();
    }

    @Override
    public String getCredentialId() {
        String apiUrl = getApiUrlParameter();
        if(apiUrl == null){ //no credentialId without apiUrl parameter
            return null;
        }
        String credentialId = createCredentialId(apiUrl);

        //check if this credentialId could be found
        StandardUsernamePasswordCredentials credential = CredentialsUtils.findCredential(credentialId, StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());
        if(credential != null){
            return credentialId;
        }
        return null;
    }

    @Override
    public Container<ScmOrganization> getOrganizations() {
        User authenticatedUser = getAuthenticatedUser();

        StaplerRequest request = Stapler.getCurrentRequest();
        Preconditions.checkNotNull(request, "This request must be made in HTTP context");

        String credentialId = getCredentialIdFromRequest(request);

        List<ErrorMessage.Error> errors = new ArrayList<>();
        StandardUsernamePasswordCredentials credential = null;
        if(credentialId == null){
            errors.add(new ErrorMessage.Error("credentialId", ErrorMessage.Error.ErrorCodes.MISSING.toString(),
                    "Missing credential id. It must be provided either as HTTP header: " + X_CREDENTIAL_ID+" or as query parameter 'credentialId'"));
        }else {
            credential = CredentialsUtils.findCredential(credentialId,
                    StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());
            if (credential == null) {
                errors.add(new ErrorMessage.Error("credentialId", ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                        String.format("credentialId: %s not found in user %s's credential store", credentialId, authenticatedUser.getId())));
            }
        }

        if(!errors.isEmpty()){
            throw new ServiceException.BadRequestException(new ErrorMessage(400, "Failed to return Bitbucket organizations").addAll(errors));
        }else {
            String apiUrl = getApiUrlParameter(request);
            BitbucketApiFactory apiFactory = BitbucketApiFactory.resolve(apiUrl);
            if (apiFactory == null) {
                throw new ServiceException.UnexpectedErrorException("BitbucketApiFactory to handle apiUrl " + apiUrl + " not found");
            }

            Preconditions.checkNotNull(credential);
            final BitbucketApi api = apiFactory.newInstance(apiUrl, credential);
            return new Container<ScmOrganization>() {
                @Override
                public ScmOrganization get(String name) {
                    return new BitbucketProject(api.getProject(name), api, getLink());
                }

                @Override
                public Link getLink() {
                    return self.rel("organizations");
                }

                @Override
                public Iterator<ScmOrganization> iterator() {
                    return iterator(0, 100);
                }

                @Override
                public Iterator<ScmOrganization> iterator(int start, int limit) {
                    return Lists.transform(api.getProjects(start, limit).getValues(), new Function<BbProject, ScmOrganization>() {
                        @Nullable
                        @Override
                        public ScmOrganization apply(@Nullable BbProject input) {
                            if (input != null) {
                                return new BitbucketProject(input, api, getLink());
                            }
                            return null;
                        }
                    }).iterator();
                }
            };
        }
    }

    /**
     * Request payload:
     * {
     *     "userName": "joe",
     *     "password":"****",
     *     "apiUrl":"mybitbucketserver.com"
     * }
     * @param request userName and password of bitbucket server
     *
     * @return credential id
     */
    @Override
    public HttpResponse validateAndCreate(@JsonBody JSONObject request) {
        User authenticatedUser = User.current();
        if(authenticatedUser == null){
            throw new ServiceException.UnauthorizedException("No logged in user found");
        }

        String userName = (String) request.get("userName");
        String password = (String) request.get("password");
        String apiUrl = (String) request.get("apiUrl");

        validate(userName, password, apiUrl);

        final StandardUsernamePasswordCredentials credential = new UsernamePasswordCredentialsImpl(CredentialsScope.USER,
                createCredentialId(apiUrl), "Bitbucket server credentials", userName, password);

        BitbucketApi api = getApi(apiUrl, credential);
        api.getUser(); //if credentials are wrong, this call will fail with 403 error

        StandardUsernamePasswordCredentials bbCredentials = CredentialsUtils.findCredential(getId(),
                StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());

        try {
            if (bbCredentials == null) {
                CredentialsUtils.createCredentialsInUserStore(
                        credential, authenticatedUser, DOMAIN_NAME,
                        ImmutableList.<DomainSpecification>of(new BlueOceanDomainSpecification()));
            } else {
                CredentialsUtils.updateCredentialsInUserStore(
                        bbCredentials, credential, authenticatedUser, DOMAIN_NAME,
                        ImmutableList.<DomainSpecification>of(new BlueOceanDomainSpecification()));
            }

            return createResponse(credential.getId());
        }catch (IOException e){
            throw new ServiceException.UnexpectedErrorException(e.getMessage());
        }
    }

    private void validate(String userName, String password, String apiUrl){
        List<ErrorMessage.Error> errorList = new ArrayList<>();
        if(StringUtils.isBlank(userName)){
            errorList.add(new ErrorMessage.Error("userName",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(),
                    "userName is required parameter"));
        }
        if(StringUtils.isBlank(password)){
            errorList.add(new ErrorMessage.Error("password",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(),
                    "password is required parameter"));
        }
        if(StringUtils.isBlank(apiUrl)){
            errorList.add(new ErrorMessage.Error("apiUrl",
                    ErrorMessage.Error.ErrorCodes.MISSING.toString(),
                    "apiUrl is required parameter"));
        }else{
            try {
                new URL(apiUrl);
            } catch (MalformedURLException e) {
                errorList.add(new ErrorMessage.Error("apiUrl",
                        ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                        "apiUrl must be a URL: "+e.getMessage()));
            }
        }
        if(!errorList.isEmpty()){
            throw new ServiceException.BadRequestException(
                    new ErrorMessage(400, "Invalid request").addAll(errorList));
        }
    }

    static BitbucketApi getApi(String apiUrl, StandardUsernamePasswordCredentials credentials){
        BitbucketApiFactory apiFactory = BitbucketApiFactory.resolve(apiUrl);
        if(apiFactory == null){
            throw new ServiceException.UnexpectedErrorException("BitbucketApiFactory to handle apiUrl "+apiUrl+" not found");
        }

        return apiFactory.newInstance(apiUrl, credentials);
    }

    private @Nonnull String createCredentialId(@Nonnull String apiUrl){
        String url = apiUrl;
        if(url.charAt(url.length()-1) != '/'){ //ensure trailing slash
            url = url + "/";
        }
        return String.format("%s:%s",getId(),DigestUtils.sha256Hex(url));
    }

    private StaplerRequest getStaplerRequest(){
        StaplerRequest request = Stapler.getCurrentRequest();
        Preconditions.checkNotNull(request, "Must be called in HTTP request context");
        return request;
    }

    private String getApiUrlParameter(){
        return getApiUrlParameter(getStaplerRequest());
    }

    private String getApiUrlParameter(StaplerRequest request){
        return request.getParameter("apiUrl");
    }

    @Extension
    public static class BbScmFactory extends ScmFactory {
        @Override
        public Scm getScm(String id, Reachable parent) {
            if(id.equals(ID)){
                return getScm(parent);
            }
            return null;
        }

        @Nonnull
        @Override
        public Scm getScm(Reachable parent) {
            return new BitbucketServerScm(parent);
        }
    }
}
