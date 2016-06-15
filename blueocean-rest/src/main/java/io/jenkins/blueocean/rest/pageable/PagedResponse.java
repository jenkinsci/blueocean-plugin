package io.jenkins.blueocean.rest.pageable;

import com.google.common.collect.Iterators;
import hudson.model.Api;
import org.kohsuke.stapler.CancelRequestHandlingException;
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

            String method = request.getMethod();
            if(!method.equalsIgnoreCase("GET")){
                throw new CancelRequestHandlingException();
            }
            final Pageable<?> resp = (Pageable<?>) target.invoke(request, response, instance, arguments);

            return new HttpResponse() {
                @Override
                public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                    int page = (req.getParameter("page") != null) ? Integer.parseInt(req.getParameter("page")) : -1;
                    int perPage = (req.getParameter("perPage") != null) ? Integer.parseInt(req.getParameter("perPage")) : -1;

                    Object[] pages;
                    if (page >= 1 && perPage >= 0) {
                        pages = Iterators.toArray(resp.iterator((page - 1) * perPage, perPage), Object.class);

                        // TODO: this is still a toy just to show the concept
                        rsp.setHeader("Link", "<" + req.getRequestURI() + "&page=" + (page+1) + ">; rel=\"next\"");
                        rsp.setHeader("Link", "<" + req.getRequestURI() + "&page=1>; rel=\"first\"");

                        if(page > 1) {
                            rsp.setHeader("Link", "<" + req.getRequestURI() + "&page=" + (page + 1) + ">; rel=\"prev\"");
                        }
                    } else {
                        pages = Iterators.toArray(resp.iterator(), Object.class);
                    }
                    new Api(pages).doJson(req, rsp);
                }
            };
        }
    }
}
