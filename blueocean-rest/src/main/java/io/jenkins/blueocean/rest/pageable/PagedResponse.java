package io.jenkins.blueocean.rest.pageable;

import com.google.common.collect.Iterators;
import hudson.model.Api;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Renders {@link Pageable} to HTTP by honoring the current page, links to next page, etc.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(METHOD)
@InterceptorAnnotation(PagedResponse.Processor.class)
public @interface PagedResponse {
    class Processor extends Interceptor {
        @Override
        public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException, ServletException {

            final Pageable<?> resp = (Pageable<?>)target.invoke(request, response, instance, arguments);

            return new HttpResponse() {
                @Override
                public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                    int start = Integer.parseInt(req.getParameter("start"));
                    int limit = Integer.parseInt(req.getParameter("limit"));

                    Object[] page = Iterators.toArray(resp.iterator(start, limit),Object.class);

                    // TODO: this is still a toy just to show the concept
                    rsp.setHeader("Link","<"+req.getRequestURI()+"&start="+(start+limit)+">; rel=\"next\"");

                    new Api(page).doJson(req, rsp);
                }
            };
        }
    }
}
