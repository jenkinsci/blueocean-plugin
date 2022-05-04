package io.jenkins.blueocean.rest.impl.pipeline.scm;

import io.jenkins.blueocean.rest.Navigable;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Resource;
import io.jenkins.blueocean.rest.pageable.Pageable;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Scm Resource
 *
 * @author Vivek Pandey
 */
public abstract class Scm extends Resource {
    public static final String ID="id";
    public static final String URI="uri";
    public static final String CREDENTIAL_ID = "credentialId";
    public static final String VALIDATE = "validate";

    public static final String X_CREDENTIAL_ID = "X-CREDENTIAL-NAME";


    /** SCM id. For example, github, bitbucket etc. */
    @Exported(name = ID)
    public abstract @Nonnull String getId();

    /** SCM URI */
    @Exported(name = URI)
    public abstract @Nonnull String getUri();

    /** credentialId attached to this scm */
    @Exported(name = CREDENTIAL_ID)
    public abstract @CheckForNull String getCredentialId();

    /**
     * Pageable list of {@link ScmOrganization}s.
     *
     * Credential Id to use with github must be provided either as credentialId query parameter or as X-CREDENTIAL-NAME http header.
     *
     * credentialId query parameter overrides X-CREDENTIAL-NAME http header.
     *
     * @return {@link Pageable} {@link ScmOrganization}s.
     */
    @Navigable
    public abstract Container<ScmOrganization> getOrganizations();

    /**
     * List of {@link ScmServerEndpoint}s.
     *
     * SCMs that do not support multiple instances can return empty list or null.
     *
     * @return list of {@link ScmServerEndpoint}s
     */
    @Navigable
    public abstract ScmServerEndpointContainer getServers();

    /**
     * Validate given credential parameters for authentication and authorization.
     *
     * Response is
     *
     * {
     *     "credentialId": "....."
     * }
     *
     * Validation of provided credential is scm dependent.
     *
     * If provided credential parameters (username, password or accessToken etc.) are not valid or do not carry
     * expected scope (such as permission to update repo, basic user info, email etc.) then an error is returned.
     *
     * If there is already a Credentials object present then it's updated with provided credential parameters,
     * otherwise new Credentials object is created and it's credential id is returned in the response.
     *
     *
     * @param request request object carrying credential parameters for this SCM
     *
     * @return credential id. If accessToken is not applicable to this SCM, null is returned.
     */
    @POST
    @WebMethod(name = VALIDATE)
    public abstract @CheckForNull HttpResponse validateAndCreate(@JsonBody JSONObject request);
}
