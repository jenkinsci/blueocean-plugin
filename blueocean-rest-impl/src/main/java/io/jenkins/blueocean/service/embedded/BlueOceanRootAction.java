package io.jenkins.blueocean.service.embedded;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import io.jenkins.blueocean.BlueOceanUI;
import io.jenkins.blueocean.BlueOceanUIProvider;
import io.jenkins.blueocean.auth.jwt.impl.JwtAuthenticationFilter;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class BlueOceanRootAction implements UnprotectedRootAction, StaplerProxy {
    private static final String URL_BASE="blue";

    private final boolean enableJWT = BlueOceanConfigProperties.BLUEOCEAN_FEATURE_JWT_AUTHENTICATION;

    @Inject
    private BlueOceanUI app;

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    /**
     * This would map to /jenkins/blue/
     */
    @Override
    public String getUrlName() {
        return URL_BASE;
    }

    @Override
    public Object getTarget() {

        StaplerRequest request = Stapler.getCurrentRequest();

        if(request.getOriginalRestOfPath().startsWith("/rest/")) {
            /**
             * If JWT is enabled, authenticate request using JWT token and set authentication context
             */
            if (enableJWT && !JwtAuthenticationFilter.didRequestHaveValidatedJwtToken()) {
                throw new ServiceException.UnauthorizedException("Unauthorized: Jwt token verification failed, no valid authentication instance found");
            }
            /**
             * Check overall read permission. This will make sure we have all rest api protected in case request
             * doesn't carry overall read permission.
             *
             * @see Jenkins#getTarget()
             */
            Authentication a = Jenkins.getAuthentication();
            if(!Jenkins.getInstance().getACL().hasPermission(a,Jenkins.READ)){
                throw new ServiceException.ForbiddenException("Forbidden");
            }
        }else{
            //If user doesn't have overall Jenkins read permission then return 403, which results in classic UI redirecting
            // user to login page
            Jenkins.getInstance().checkPermission(Jenkins.READ);
        }

        // frontend uses this to determine when to reload
        Stapler.getCurrentResponse().setHeader("X-Blueocean-Refresher", Jenkins.SESSION_HASH);

        return app;
    }

    /** Provides implementation of BlueOceanUI */
    @Extension
    public static class ModuleImpl implements Module {

        @Override
        public void configure(Binder binder) {
            binder.bind(BlueOceanUI.class).toInstance(new BlueOceanUI());
        }
    }

    @Extension(ordinal = -9999)
    public static class BlueOceanUIProviderImpl extends BlueOceanUIProvider {
        @Override
        public String getRootUrl() {
            return Jenkins.getInstance().getRootUrl();
        }

        @NonNull
        @Override
        public String getUrlBasePrefix() {
            return URL_BASE;
        }

        @NonNull
        @Override
        public String getLandingPagePath() {
            BlueOrganization organization = OrganizationFactory.getInstance().getContainingOrg(Jenkins.getInstance());
            String orgName = organization != null ? organization.getName() : "jenkins";
            return String.format("/organizations/%s/pipelines/", orgName);
        }
    }
}
