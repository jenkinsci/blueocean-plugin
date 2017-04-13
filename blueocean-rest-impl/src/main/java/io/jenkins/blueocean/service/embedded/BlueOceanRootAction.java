package io.jenkins.blueocean.service.embedded;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.remoting.Base64;
import io.jenkins.blueocean.BlueOceanUI;
import io.jenkins.blueocean.BlueOceanUIProvider;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueOrganizationContainer;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class BlueOceanRootAction implements UnprotectedRootAction, StaplerProxy {
    private static final String URL_BASE="blue";
    private static final Long randomBits = new Random().nextLong();

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
            if(enableJWT) {
                Authentication tokenAuthentication = JwtAuthenticationToken.create(request);

                //create a new context and set it to holder to not clobber existing context
                SecurityContext securityContext = new SecurityContextImpl();
                securityContext.setAuthentication(tokenAuthentication);
                SecurityContextHolder.setContext(securityContext);

                //TODO: implement this as filter, see PluginServletFilter to clear the context
            } else {
                HashCode hashCode = Hashing.sha1()
                    .newHasher()
                    .putString(Jenkins.getAuthentication().getName(), StandardCharsets.UTF_8)
                    .putLong(randomBits)
                    .hash();

                // Base64 encode to ensure no non-ASCII characters get into the header
                String refresherToken = Base64.encode(hashCode.asBytes());
                Stapler.getCurrentResponse().setHeader("X-Blueocean-Refresher", refresherToken);
            }
        }else{
            //If user doesn't have overall Jenkins read permission then return 403, which results in classic UI redirecting
            // user to login page
            Jenkins.getInstance().checkPermission(Jenkins.READ);
        }

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
