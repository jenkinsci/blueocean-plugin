package io.jenkins.blueocean;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import hudson.Plugin;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.Util;
import jenkins.model.Jenkins;
import jnr.ffi.annotations.In;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Plugin-aware resource management system
 */
@Singleton
public class ResourceManager {

    /**
     * Map used as a set to remember which resources can be served.
     */
    private final ConcurrentHashMap<String,String> allowedResources = new ConcurrentHashMap<>();

    @Inject
    private PluginManager pluginManager;

    @Inject
    private ServletContext context;

    private static final String KEY = ResourceManager.class.getName();

    public ResourceManager() {
        install();
    }

    /**
     * Handle URLs like /resources/my-plugin/c82be88/org/jenkins-ci/plugins/bundle.js
     * @param req request
     * @param rsp response
     * @throws IOException on error
     * @throws ServletException on error
     */
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String path = req.getRestOfPath();
        if (path.charAt(0)=='/') path = path.substring(1);

        String[] atoms = path.split("/");
        if (atoms.length < 3) {
            throw HttpResponses.error(SC_BAD_REQUEST, new IllegalArgumentException("Request not valid"));
        }
        String pluginName = atoms[0];
        String resourceHash = atoms[1];

        if(!allowedResources.containsKey(path)) {
            if(!allowResourceToBeServed(path)) {
                rsp.sendError(SC_FORBIDDEN);
                return;
            }
            // remember URLs that we can serve. but don't remember error ones, as it might be unbounded
            allowedResources.put(path,path);
        }

        PluginWrapper plugin = pluginManager.getPlugin(pluginName);
        if (plugin == null) {
            throw HttpResponses.error(SC_NOT_FOUND, new IllegalArgumentException("No such plugin found " + pluginName));
        }

        if (!calculateCacheHash(plugin).equals(resourceHash)) {
            throw HttpResponses.error(SC_NOT_FOUND, new IllegalArgumentException("Requested resource does not exist: " + path));
        }

        if (plugin.classLoader == null) {
            throw HttpResponses.error(SC_NOT_FOUND, new IllegalArgumentException("Plugin " + pluginName + " is disabled"));
        }

        String resourcePath = StringUtils.join(Arrays.copyOfRange(atoms, 2, atoms.length), '/');
        URL resource = plugin.classLoader.getResource(resourcePath);

        if(resource == null) {
            throw HttpResponses.error(SC_NOT_FOUND,new IllegalArgumentException("No such adjunct found: "+path));
        } else {
            long expires = MetaClass.NO_CACHE ? 0 : TimeUnit.DAYS.toMillis(90);
            rsp.serveFile(req,resource,expires);
        }
    }

    /**
     * Creates a unique hash of the plugin version and the Jenkins version to be used as a cache buster
     * @param plugin to hash
     * @return the cache busting atom
     */
    public String calculateCacheHash(PluginWrapper plugin) {
        return Util.getDigestOf(plugin.getVersion() + ":" + Jenkins.getVersion().toString());
    }

    boolean allowResourceToBeServed(String absolutePath) {
        // does it have an adjunct directory marker?
        int idx = absolutePath.lastIndexOf('/');
        if (idx>0 && pluginManager.uberClassLoader.getResource(absolutePath.substring(0,idx)+"/.adjunct")!=null)
            return true;

        // backward compatible behaviour
        return absolutePath.endsWith(".gif")
            || absolutePath.endsWith(".png")
            || absolutePath.endsWith(".css")
            || absolutePath.endsWith(".js");
    }

    public static ResourceManager get(ServletContext context) {
        return (ResourceManager) context.getAttribute(KEY);
    }

    private void install() {
        if (get(context) == null) {
            context.setAttribute(KEY, this);
        }
    }
}
