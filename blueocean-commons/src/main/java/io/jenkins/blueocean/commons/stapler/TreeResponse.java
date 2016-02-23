package io.jenkins.blueocean.commons.stapler;

import hudson.model.Api;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.DataWriter;
import org.kohsuke.stapler.export.ExportConfig;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.ModelBuilder;
import org.kohsuke.stapler.export.NamedPathPruner;
import org.kohsuke.stapler.export.TreePruner;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Takes the return value of a web method and serve it as a JSON representation
 * via {@link Api} class. Among other things, this enables the tree parameter.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(METHOD)
@InterceptorAnnotation(TreeResponse.Processor.class)
public @interface TreeResponse {
    class Processor extends Interceptor {
        @Override
        public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException {

            final Object resp = target.invoke(request, response, instance, arguments);

            return new HttpResponse() {
                @Override
                public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                    if (node instanceof Object[]) {
                        // TODO: Kohsuke is going to make this array handling a part of Stapler
                        Flavor flavor = Flavor.JSON;
                        rsp.setContentType(flavor.contentType);
                        Writer w = rsp.getCompressedWriter(req);
                
                        TreePruner pruner = null;
                        String tree = req.getParameter("tree");
                        if (tree != null) {
                            try {
                                pruner = new NamedPathPruner(tree);
                            } catch (IllegalArgumentException x) {
                                throw new ServletException("Malformed tree expression: " + x, x);
                            }
                        }
                
                        ExportConfig config = new ExportConfig();
                        config.prettyPrint = req.hasParameter("pretty");

                        DataWriter dw = flavor.createDataWriter(node, w, config);
                        dw.startArray();
                        for (Object item : (Object[])node) {
                            Model p = MODEL_BUILDER.get(item.getClass());
                            p.writeTo(item, pruner, dw);
                        }
                        dw.endArray();
                        w.close();
                    } else {
                        new Api(resp).doJson(req, rsp);
                    }
                }
            };
        }
        private static final ModelBuilder MODEL_BUILDER = new ModelBuilder();
    }
}
