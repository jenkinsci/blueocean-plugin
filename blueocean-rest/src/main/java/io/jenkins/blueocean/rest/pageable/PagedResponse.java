package io.jenkins.blueocean.rest.pageable;

import com.google.common.collect.Iterators;
import io.jenkins.blueocean.commons.stapler.Export;
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
import java.util.Arrays;
import java.util.List;

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
    public static final int DEFAULT_LIMIT=100;
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
                    int start = (req.getParameter("start") != null) ? Integer.parseInt(req.getParameter("start")) : 0;
                    int limit = (req.getParameter("limit") != null) ? Integer.parseInt(req.getParameter("limit")) : DEFAULT_LIMIT;

                    if(start < 0){
                        start = 0;
                    }

                    if(limit < 0){
                        limit = DEFAULT_LIMIT;
                    }
                    Object[] page = Iterators.toArray(resp.iterator(start, limit), Object.class);
                    String url = req.getOriginalRequestURI();

                    String separator = "?";
                    if(req.getQueryString() != null){
                        String q = getQueryString(req.getQueryString(), "start", "limit");
                        if(q.length()>0){
                            url += "?"+q;
                            separator = "&";
                        }
                    }
                    rsp.setHeader("Link", "<" + url + separator + "start=" + (start + limit) + "&limit="+limit + ">; rel=\"next\"");

                    Export.doJson(req, rsp, page);
                }
            };
        }

        private String getQueryString(String query, String... excludes){
            List<String> excludeList = Arrays.asList(excludes);
            String[] values = query.split("&");

            StringBuilder sb = new StringBuilder();

            for (String v : values) {
                String[] vv = v.split("=");
                if (vv.length != 2 || excludeList.contains(vv[0].trim())) {
                    continue;
                }
                if(sb.length() > 0){
                    sb.append("&");
                }
                sb.append(vv[0].trim()).append("=").append(vv[1].trim());
            }

            return sb.toString();
        }


    }
}
