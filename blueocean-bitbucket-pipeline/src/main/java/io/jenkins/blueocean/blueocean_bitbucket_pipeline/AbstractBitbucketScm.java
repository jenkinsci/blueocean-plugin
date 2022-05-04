package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.jenkins.plugins.bitbucket.endpoints.BitbucketEndpointConfiguration;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.User;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainSpecification;
import io.jenkins.blueocean.rest.impl.pipeline.scm.AbstractScm;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmOrganization;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.pageable.PagedResponse;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.json.JsonBody;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author Vivek Pandey
 */
public abstract class AbstractBitbucketScm extends AbstractScm {
    private final Reachable parent;

    public AbstractBitbucketScm(Reachable parent) {
        this.parent = parent;
    }

    @Override
    public Object getState() {
        StaplerRequest request = Stapler.getCurrentRequest();
        Objects.requireNonNull(request, "Must be called in HTTP request context");
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method)) {
            throw new ServiceException.MethodNotAllowedException(String.format("Request method %s is not allowed", method));
        }

        checkPermission();

        String apiUrl = request.getParameter("apiUrl");

        ErrorMessage message = new ErrorMessage(400, "Invalid request");
        if(StringUtils.isBlank(apiUrl)) {
            message.add(new ErrorMessage.Error("apiUrl", ErrorMessage.Error.ErrorCodes.MISSING.toString(),
                    "apiUrl is required parameter"));
        }
        try {
            new URL(apiUrl);
        } catch (MalformedURLException e) {
            message.add(new ErrorMessage.Error("apiUrl", ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                    "apiUrl parameter must be a valid URL"));
        }
        if(!message.getErrors().isEmpty()){
            throw new ServiceException.BadRequestException(message);
        }
        validateExistingCredential(apiUrl);
        return super.getState();
    }

    @Override
    public Link getLink() {
        return parent.getLink().rel(getId());
    }

    @Nonnull
    @Override
    public String getUri() {
        return getApiUrlParameter();
    }

    @Override
    public String getCredentialId() {
        String apiUrl = getApiUrlParameter();
        String credentialId = createCredentialId(apiUrl);

        //check if this credentialId could be found
        StandardUsernamePasswordCredentials credential = CredentialsUtils.findCredential(credentialId, StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());
        if(credential != null){
            return credentialId;
        }
        return null;
    }

    StandardUsernamePasswordCredentials getCredential(String apiUrl){
        String credentialId = createCredentialId(apiUrl);
        return CredentialsUtils.findCredential(credentialId,
                StandardUsernamePasswordCredentials.class,
                new BlueOceanDomainRequirement());
    }

    @Override
    public Container<ScmOrganization> getOrganizations() {
        StaplerRequest request = Stapler.getCurrentRequest();
        Objects.requireNonNull(request, "This request must be made in HTTP context");
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method)) {
            throw new ServiceException.MethodNotAllowedException(String.format("Request method %s is not allowed", method));
        }

        User authenticatedUser = getAuthenticatedUser();
        checkPermission();

        String credentialId = BitbucketCredentialUtils.computeCredentialId(getCredentialIdFromRequest(request), getId(), getUri());

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
        String apiUrl  =  request.getParameter("apiUrl");
        if(StringUtils.isBlank(apiUrl)){
            errors.add(new ErrorMessage.Error("apiUrl", ErrorMessage.Error.ErrorCodes.MISSING.toString(),
                    "apiUrl is required parameter"));
        }
        if(!errors.isEmpty()){
            throw new ServiceException.BadRequestException(new ErrorMessage(400, "Failed to return Bitbucket organizations").addAll(errors));
        }else {
            apiUrl = normalizeApiUrl(apiUrl);
            BitbucketApiFactory apiFactory = BitbucketApiFactory.resolve(this.getId());
            if (apiFactory == null) {
                throw new ServiceException.UnexpectedErrorException("BitbucketApiFactory to handle apiUrl " + apiUrl + " not found");
            }

            Objects.requireNonNull(credential);
            final BitbucketApi api = apiFactory.create(apiUrl, credential);
            return new Container<ScmOrganization>() {
                @Override
                public ScmOrganization get(String name) {
                    return new BitbucketOrg(api.getOrg(name), api, getLink());
                }

                @Override
                public Link getLink() {
                    return AbstractBitbucketScm.this.getLink().rel("organizations");
                }

                @Override
                public Iterator<ScmOrganization> iterator() {
                    return iterator(0, 100);
                }

                @Override
                public Iterator<ScmOrganization> iterator(int start, int limit) {
                    if(limit <= 0){
                        limit = PagedResponse.DEFAULT_LIMIT;
                    }
                    if(start <0){
                        start = 0;
                    }
                    int page =  (start/limit) + 1;
                    return api.getOrgs(page, limit).getValues().stream().map( input -> {
                        if (input != null) {
                            return (ScmOrganization) new BitbucketOrg(input, api, getLink());
                        }
                        return null;
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
        checkPermission();

        String userName = (String) request.get("userName");
        String password = (String) request.get("password");
        String apiUrl = (String) request.get("apiUrl");

        validate(userName, password, apiUrl);

        final StandardUsernamePasswordCredentials credential = new UsernamePasswordCredentialsImpl(CredentialsScope.USER,
                createCredentialId(apiUrl), "Bitbucket server credentials", userName, password);

        //if credentials are wrong, this call will fail with 401 error
        validateCredential(apiUrl, credential);

        StandardUsernamePasswordCredentials bbCredentials = CredentialsUtils.findCredential(createCredentialId(apiUrl),
                StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());

        try {
            if (bbCredentials == null) {
                CredentialsUtils.createCredentialsInUserStore(
                        credential, authenticatedUser, getDomainId(),
                        Collections.singletonList(new BlueOceanDomainSpecification()));
            } else {
                CredentialsUtils.updateCredentialsInUserStore(
                        bbCredentials, credential, authenticatedUser, getDomainId(),
                        Collections.singletonList(new BlueOceanDomainSpecification()));
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

    /**
     * Validate that the credential is valid for the specified apiUrl.
     * Will throw a 401 ServiceException if the credential is invalid.
     * @param apiUrl
     * @param credential
     */
    private void validateCredential(String apiUrl, StandardUsernamePasswordCredentials credential) {
        try {
            BitbucketApi api = getApi(apiUrl, this.getId(), credential);
            api.getUser(); //if credentials are wrong, this call will fail with 401 error
        } catch (ServiceException ex) {
            if (ex.status == 401) {
                throw new ServiceException.UnauthorizedException(
                    new ErrorMessage(401, "Invalid username / password")
                );
            } else {
                throw ex;
            }

        }
    }

    /**
     * Validate that the existing credential is valid (if it exists).
     * Will throw a 401 ServiceException if the credential is invalid.
     * @param apiUrl
     */
    private void validateExistingCredential(@Nonnull String apiUrl) {
        StandardUsernamePasswordCredentials bbCredentials = CredentialsUtils.findCredential(createCredentialId(apiUrl),
            StandardUsernamePasswordCredentials.class, new BlueOceanDomainRequirement());

        if (bbCredentials != null) {
            try {
                validateCredential(apiUrl, bbCredentials);
            } catch (ServiceException ex) {
                if (ex.status == 401) {
                    throw new ServiceException.UnauthorizedException(
                        new ErrorMessage(401, "Existing credential failed authorization")
                    );
                } else {
                    throw ex;
                }
            }
        }
    }

    public static BitbucketApi getApi(String apiUrl, String scmId, StandardUsernamePasswordCredentials credentials){
        BitbucketApiFactory apiFactory = BitbucketApiFactory.resolve(scmId);
        if(apiFactory == null){
            throw new ServiceException.UnexpectedErrorException("BitbucketApiFactory to handle apiUrl "+apiUrl+" not found");
        }

        return apiFactory.create(apiUrl, credentials);
    }

    /**
     *  Caller must ensure apiUrl is not blank or null
     *
     * @param apiUrl must be normalized url using {@link BitbucketEndpointConfiguration#normalizeServerUrl(String)}
     * @return url
     */
    protected  abstract @Nonnull String createCredentialId(@Nonnull String apiUrl);

    protected abstract @Nonnull String getDomainId();

    protected StaplerRequest getStaplerRequest(){
        StaplerRequest request = Stapler.getCurrentRequest();
        Objects.requireNonNull(request, "Must be called in HTTP request context");
        return request;
    }

    protected @Nonnull String getApiUrlParameter(){
        return getApiUrlParameter(getStaplerRequest());
    }

    private @Nonnull String getApiUrlParameter(StaplerRequest request){
        String apiUrl =  request.getParameter("apiUrl");
        // Ensure apiUrl is not blank/null, otherwise BitbucketEndpointConfiguration.normalizeServerUrl() will
        // return bitbucket cloud API
        if(StringUtils.isBlank(apiUrl)){
            throw new ServiceException.BadRequestException("apiUrl is required parameter");
        }
        return normalizeApiUrl(apiUrl);
    }

    @Restricted(NoExternalUse.class)
    public static @Nonnull String normalizeApiUrl(@Nonnull String apiUrl){
        return BitbucketEndpointConfiguration.normalizeServerUrl(apiUrl);
    }
}
