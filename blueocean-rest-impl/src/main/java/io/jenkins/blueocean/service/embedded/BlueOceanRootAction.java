package io.jenkins.blueocean.service.embedded;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import io.jenkins.blueocean.BlueOceanUI;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class BlueOceanRootAction implements UnprotectedRootAction, StaplerProxy {
    private static final String URL_BASE="blue";

    private final boolean disableJWT = Boolean.getBoolean("DISABLE_BLUEOCEAN_JWT_AUTHENTICATION");

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

        if(!disableJWT && request.getOriginalRestOfPath().startsWith("/rest/")) {
            JwtAuthenticationToken tokenAuthentication = new JwtAuthenticationToken(request);
            SecurityContext holder = SecurityContextHolder.getContext();
            holder.setAuthentication(tokenAuthentication);
        }
        return app;
    }

    /** Provides implementation of BlueOceanUI */
    @Extension
    public static class ModuleImpl implements Module {

        @Override
        public void configure(Binder binder) {
            binder.bind(BlueOceanUI.class).toInstance(new BlueOceanUI(URL_BASE));
        }
    }
}
