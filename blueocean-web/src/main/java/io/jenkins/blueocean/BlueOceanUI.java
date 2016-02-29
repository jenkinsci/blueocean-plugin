package io.jenkins.blueocean;

import hudson.Extension;
import hudson.ExtensionList;
import io.jenkins.blueocean.jsextensions.JenkinsJSExtensions;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Root of Blue Ocean UI
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class BlueOceanUI {
    /**
     * Exposes {@link RootRoutable}s to the URL space.
     */
    public RootRoutable getDynamic(String route) {
        for (RootRoutable r : ExtensionList.lookup(RootRoutable.class)) {
            if (r.getUrlName().equals(route))
                return r;
        }
        return null;
    }

    // TODO: Look into using new Stapler stuff for doing this.
    @Restricted(DoNotUse.class)
    public HttpResponse doJavaScriptExtensionInfo() {
        byte[] responseData = JenkinsJSExtensions.getJenkinsJSExtensionData();
        return new JsonResponse(responseData);
    }

    private class JsonResponse implements HttpResponse {

        private final byte[] data;

        public JsonResponse(byte[] data) {
            this.data = data;
        }

        @Override
        public void generateResponse(StaplerRequest staplerRequest, StaplerResponse staplerResponse, Object o) throws IOException, ServletException {
            staplerResponse.setContentType("application/json; charset=UTF-8");
            staplerResponse.setContentLength(data.length);
            staplerResponse.getOutputStream().write(data);
        }
    };
}
