package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.Descriptor;
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
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.json.JsonBody;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
        StaplerRequest2 request = Stapler.getCurrentRequest2();
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

    @NonNull
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
        StaplerRequest2 request = Stapler.getCurrentRequest2();
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

        final StandardUsernamePasswordCredentials credential;
        try {
            credential = new UsernamePasswordCredentialsImpl(CredentialsScope.USER,
                createCredentialId(apiUrl), "Bitbucket server credentials", userName, password);
        } catch (Descriptor.FormException e) {
            throw new RuntimeException(e);
        }

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
    private void validateExistingCredential(@NonNull String apiUrl) {
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
     * @param apiUrl to get the associated credentialId
     * @return url
     */
    protected  abstract @NonNull String createCredentialId(@NonNull String apiUrl);

    protected abstract @NonNull String getDomainId();

    protected StaplerRequest2 getStaplerRequest2(){
        StaplerRequest2 request = Stapler.getCurrentRequest2();
        Objects.requireNonNull(request, "Must be called in HTTP request context");
        return request;
    }

    protected @NonNull String getApiUrlParameter(){
        return getApiUrlParameter(getStaplerRequest2());
    }

    private @NonNull String getApiUrlParameter(StaplerRequest2 request){
        String apiUrl =  request.getParameter("apiUrl");
        // Ensure apiUrl is not blank/null
        if(StringUtils.isBlank(apiUrl)){
            throw new ServiceException.BadRequestException("apiUrl is required parameter");
        }
        return normalizeApiUrl(apiUrl);
    }

    @Restricted(NoExternalUse.class)
    public static @NonNull String normalizeApiUrl(@NonNull String apiUrl){
        try {
            URI uri = new URI(apiUrl).normalize();
            String scheme = uri.getScheme();
            if ("http".equals(scheme) || "https".equals(scheme)) {
                // we only expect http / https, but also these are the only ones where we know the authority
                // is server based, i.e. [userinfo@]server[:port]
                // DNS names must be US-ASCII and are case insensitive, so we force all to lowercase

                String host = uri.getHost() == null ? null : uri.getHost().toLowerCase(Locale.ENGLISH);
                int port = uri.getPort();
                if ("http".equals(scheme) && port == 80) {
                    port = -1;
                } else if ("https".equals(scheme) && port == 443) {
                    port = -1;
                }
                apiUrl = new URI(
                        scheme,
                        uri.getUserInfo(),
                        host,
                        port,
                        uri.getPath(),
                        uri.getQuery(),
                        uri.getFragment()
                ).toASCIIString();
            }
        } catch (URISyntaxException e) {
            // ignore, this was a best effort tidy-up
        }
        String serverUrl = apiUrl.replaceAll("/$", "");
        if(serverUrl == null){
            throw new ServiceException.UnexpectedErrorException("apiUrl is empty or is not a valid URL");
        }
        return apiUrl;
    }
}
