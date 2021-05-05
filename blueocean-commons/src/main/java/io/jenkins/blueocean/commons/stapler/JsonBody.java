package io.jenkins.blueocean.commons.stapler;

import io.jenkins.blueocean.commons.JsonConverter;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AnnotationHandler;
import org.kohsuke.stapler.InjectedParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Binds the body payload into POJO via json-lib.
 *
 * <p>
 * On a web-bound {@code doXyz} method, use this method on a parameter to get the content of the request
 * data-bound to a bean through {@link JSONObject#fromObject(Object)} and inject it as a parameter.
 * For example,
 *
 * <pre>
 * &#64;JsonResponse
 * public Point doDouble(@JsonBody Point p) {
 *   Point pt = new Point();
 *   pt.x = p.x*2;
 *   pt.y = p.y*2;
 *   return pt;
 * }
 *
 * public class Point { public int x, y; }
 * </pre>
 *
 * Request:
 * <pre>
 * POST ..../double
 * Content-Type: application/json
 *
 * {x:10,y:5}
 * </pre>
 *
 * Response:
 * <pre>
 * 200 OK
 * Content-Type: application/json;charset=UTF-8
 *
 * {x:20,y:10}
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 */
@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
@InjectedParameter(JsonBody.Handler.class)
public @interface JsonBody {
    class Handler extends AnnotationHandler {
        @Override
        public Object parse(StaplerRequest request, Annotation a, Class type, String parameterName) throws ServletException {
            /*
                TODO: exception thrown here results in error page rendered in HTML.
                To fix this, change oops.jelly in Jenkins to recognize the Accept header and serve JSON if
                that's what the client wants. See web.xml <error-page> declaration.
             */

            String ct = request.getContentType();
            if ((ct == null) || !ct.startsWith("application/json"))
                throw new ServletException("Expected application/json but got "+ct);

            try {
                return JsonConverter.toJava(request.getReader(), type);
            } catch (IOException e) {
                throw new ServletException("Failed to read JSON",e);
            }
        }
    }
}
