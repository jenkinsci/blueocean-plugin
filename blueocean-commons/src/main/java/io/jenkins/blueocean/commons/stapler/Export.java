package io.jenkins.blueocean.commons.stapler;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.PluginWrapper;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Run;
import io.jenkins.blueocean.commons.stapler.export.DataWriter;
import io.jenkins.blueocean.commons.stapler.export.ExportConfig;
import io.jenkins.blueocean.commons.stapler.export.ExportInterceptor;
import io.jenkins.blueocean.commons.stapler.export.Flavor;
import io.jenkins.blueocean.commons.stapler.export.Model;
import io.jenkins.blueocean.commons.stapler.export.ModelBuilder;
import io.jenkins.blueocean.commons.stapler.export.NamedPathPruner;
import io.jenkins.blueocean.commons.stapler.export.Property;
import io.jenkins.blueocean.commons.stapler.export.TreePruner;
import io.jenkins.blueocean.commons.stapler.export.TreePruner.ByDepth;
import jenkins.model.Jenkins;
import jenkins.security.SecureRequester;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;

public class Export {

    private static final Logger logger = LoggerFactory.getLogger(Export.class);

    private static ModelBuilder MODEL_BUILDER = new ModelBuilder();

    /**
     * Serialize the supplied object to JSON and return as a {@link String}.
     * @param object The object to serialize.
     * @return The JSON as a {@link String}.
     * @throws IOException Error serializing model object.
     */
    @NonNull
    public static String toJson(@NonNull Object object) throws IOException {
        return toJson(object, false);
    }

    /**
     * Serialize the supplied object to JSON and return as a {@link String}.
     * @param object The object to serialize.
     * @param htmlEncoded enable html encoding so its safe to output to html
     * @return The JSON as a {@link String}.
     * @throws IOException Error serializing model object.
     */
    @NonNull
    public static String toJson(@NonNull Object object, boolean htmlEncoded) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            toJson(object, writer, htmlEncoded);
            return writer.toString();
        }
    }

    /**
     * Serialize the supplied object to JSON and write to the supplied {@link Writer}.
     * @param object The object to serialize.
     * @param writer The writer to output to.
     * @throws IOException Error serializing model object.
     */
    @SuppressWarnings("unchecked")
    public static void toJson(@NonNull Object object, @NonNull Writer writer) throws IOException {
        toJson(object, writer, false);
    }


    /**
     * Serialize the supplied object to JSON and write to the supplied {@link Writer}.
     * @param object The object to serialize.
     * @param writer The writer to output to.
     * @param htmlEncoded enable html encoding so its safe to output to html
     * @throws IOException Error serializing model object.
     */
    @SuppressWarnings("unchecked")
    public static void toJson(@NonNull Object object, @NonNull Writer writer, boolean htmlEncoded) throws IOException {
        Model model = new ModelBuilder().get(object.getClass());
        ExportConfig exportConfig = createExportConfig();
        if (htmlEncoded) {
            exportConfig.withHtmlEncode(true);
        }
        model.writeTo(object, Flavor.JSON.createDataWriter(object, writer, exportConfig));
        writer.flush();
    }

    /**
     * @param req request
     * @param rsp response
     * @param bean to serve
     * @throws IOException if cannot be written
     * @throws ServletException if something goes wrong processing the request
     */
    public static void doJson(StaplerRequest req, StaplerResponse rsp, Object bean) throws IOException, ServletException {
        if (req.getParameter("jsonp") == null || permit(req, bean)) {
            rsp.setHeader("X-Jenkins", Jenkins.VERSION);
            rsp.setHeader("X-Jenkins-Session", Jenkins.SESSION_HASH);
            ExportConfig exportConfig = createExportConfig()
                    .withFlavor(req.getParameter("jsonp") == null ? Flavor.JSON : Flavor.JSONP)
                    .withPrettyPrint(req.hasParameter("pretty")).withSkipIfFail(true);
            serveExposedBean(req, rsp, bean, exportConfig);
        } else {
            rsp.sendError(HttpURLConnection.HTTP_FORBIDDEN, "jsonp forbidden; implement jenkins.security.SecureRequester");
        }
    }

    private static ExportConfig createExportConfig() {
        return new ExportConfig().withExportInterceptor(new BlueOceanExportInterceptor());
    }

    private static boolean permit(StaplerRequest req, Object bean) {
        for (SecureRequester r : ExtensionList.lookup(SecureRequester.class)) {
            if (r.permit(req, bean)) {
                return true;
            }
        }
        return false;
    }

    private static void serveExposedBean(StaplerRequest req, StaplerResponse resp, Object exposedBean, ExportConfig config) throws ServletException, IOException {
        Flavor flavor = config.getFlavor();
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

    private Export() {}

    private static class BlueOceanExportInterceptor extends ExportInterceptor{
        @Override
        public Object getValue(Property property, Object model, ExportConfig config) throws IOException {
            if(model instanceof Action){
                try {
                    return property.getValue(model);
                } catch (Throwable e) {
                    printError(model.getClass(), e);
                    return SKIP;
                }
            } else if (model instanceof Item || model instanceof Run) {
                // We should skip any models that are Jenkins Item or Run objects as these are known to be evil
                return SKIP;
            }
            return ExportInterceptor.DEFAULT.getValue(property, model, config);
        }

        private void printError(Class modelClass, Throwable e){
            PluginWrapper plugin = Jenkins.get().getPluginManager().whichPlugin(modelClass);
            String msg;
            if (plugin != null) {
                String url = plugin.getUrl() == null ? "https://issues.jenkins-ci.org/" : plugin.getUrl();
                msg = "BUG: Problem with serializing <" + modelClass + "> belonging to plugin <" + plugin.getLongName() + ">. Report the stacktrace below to the plugin author by visiting " + url;
            } else {
                msg = "BUG: Problem with serializing <" + modelClass + ">";
            }
            if(e != null) {
                logger.error(msg, e);
            }else{
                logger.error(msg);
            }
        }
    }
}
