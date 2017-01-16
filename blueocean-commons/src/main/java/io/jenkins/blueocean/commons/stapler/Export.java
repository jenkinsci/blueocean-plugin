package io.jenkins.blueocean.commons.stapler;

import hudson.ExtensionList;
import io.jenkins.blueocean.commons.stapler.export.DataWriter;
import io.jenkins.blueocean.commons.stapler.export.ExportConfig;
import io.jenkins.blueocean.commons.stapler.export.Flavor;
import io.jenkins.blueocean.commons.stapler.export.Model;
import io.jenkins.blueocean.commons.stapler.export.ModelBuilder;
import io.jenkins.blueocean.commons.stapler.export.NamedPathPruner;
import io.jenkins.blueocean.commons.stapler.export.TreePruner;
import io.jenkins.blueocean.commons.stapler.export.TreePruner.ByDepth;
import jenkins.model.Jenkins;
import jenkins.security.SecureRequester;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;
import java.net.HttpURLConnection;

public class Export {

    static ModelBuilder MODEL_BUILDER = new ModelBuilder();

    public static void doJson(StaplerRequest req, StaplerResponse rsp, Object bean) throws IOException, ServletException {
        if (req.getParameter("jsonp") == null || permit(req, bean)) {
            rsp.setHeader("X-Jenkins", Jenkins.VERSION);
            rsp.setHeader("X-Jenkins-Session", Jenkins.SESSION_HASH);
            serveExposedBean(req, rsp, bean, req.getParameter("jsonp") == null ? Flavor.JSON : Flavor.JSONP);
        } else {
            rsp.sendError(HttpURLConnection.HTTP_FORBIDDEN, "jsonp forbidden; implement jenkins.security.SecureRequester");
        }
    }

    private static boolean permit(StaplerRequest req, Object bean) {
        for (SecureRequester r : ExtensionList.lookup(SecureRequester.class)) {
            if (r.permit(req, bean)) {
                return true;
            }
        }
        return false;
    }

    private static void serveExposedBean(StaplerRequest req, StaplerResponse resp, Object exposedBean, Flavor flavor) throws ServletException, IOException {
        String pad=null;
        resp.setContentType(flavor.contentType);
        Writer w = resp.getCompressedWriter(req);

        if (flavor== Flavor.JSON || flavor== Flavor.JSONP) { // for compatibility reasons, accept JSON for JSONP as well.
            pad = req.getParameter("jsonp");
            if(pad!=null) w.write(pad+'(');
        }

        TreePruner pruner;
        String tree = req.getParameter("tree");
        if (tree != null) {
            try {
                pruner = new NamedPathPruner(tree);
            } catch (IllegalArgumentException x) {
                throw new ServletException("Malformed tree expression: " + x, x);
            }
        } else {
            int depth = 0;
            try {
                String s = req.getParameter("depth");
                if (s != null) {
                    depth = Integer.parseInt(s);
                }
            } catch (NumberFormatException e) {
                throw new ServletException("Depth parameter must be a number");
            }
            pruner = new ByDepth(1 - depth);
        }

        ExportConfig config = new ExportConfig();
        config.prettyPrint = req.hasParameter("pretty");

        DataWriter dw = flavor.createDataWriter(exposedBean, w, config);
        if (exposedBean instanceof Object[]) {
            // TODO: extend the contract of DataWriter to capture this
            // TODO: make this work with XML flavor (or at least reject this better)
            dw.startArray();
            for (Object item : (Object[])exposedBean)
                writeOne(pruner, dw, item);
            dw.endArray();
        } else {
            writeOne(pruner, dw, exposedBean);
        }

        if(pad!=null) w.write(')');
        w.close();
    }

    private static void writeOne(TreePruner pruner, DataWriter dw, Object item) throws IOException {
        Model p = MODEL_BUILDER.get(item.getClass());
        p.writeTo(item, pruner, dw);
    }

    private Export() {};
}
