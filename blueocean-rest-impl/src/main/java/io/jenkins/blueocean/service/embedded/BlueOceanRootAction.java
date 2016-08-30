package io.jenkins.blueocean.service.embedded;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import io.jenkins.blueocean.BlueOceanUI;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
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
            Authentication tokenAuthentication = JwtAuthenticationToken.create(request);

            //create a new context and set it to holder to not clobber existing context
            SecurityContext securityContext = new SecurityContextImpl();
            securityContext.setAuthentication(tokenAuthentication);
            SecurityContextHolder.setContext(securityContext);

            //TODO: implement this as filter, see PluginServletFilter to clear the context
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
