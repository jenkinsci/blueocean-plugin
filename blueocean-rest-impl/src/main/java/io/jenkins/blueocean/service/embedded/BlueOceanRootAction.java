package io.jenkins.blueocean.service.embedded;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import io.jenkins.blueocean.BlueOceanUI;
import io.jenkins.blueocean.BlueOceanUIProvider;
import io.jenkins.blueocean.auth.jwt.impl.JwtAuthenticationFilter;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueOrganizationContainer;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

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
            if (enableJWT && !JwtAuthenticationFilter.didRequestHaveValidatedJwtToken()) {
                throw new ServiceException.UnauthorizedException("Unauthorized: Jwt token verification failed, no valid authentication instance found");
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

        @Nonnull
        @Override
        public String getUrlBasePrefix() {
            return URL_BASE;
        }

        @Nonnull
        @Override
        public String getLandingPagePath() {
            BlueOrganization organization = BlueOrganizationContainer.getBlueOrganization();
            String orgName = organization != null ? organization.getName() : "jenkins";
            return String.format("/organizations/%s/pipelines/", orgName);
        }
    }
}
