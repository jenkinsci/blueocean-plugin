package io.jenkins.blueocean.commons.stapler;

import hudson.model.Api;
import org.kohsuke.stapler.CancelRequestHandlingException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;
import org.kohsuke.stapler.verb.HttpVerbInterceptor;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

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

        private static final Pattern SCM_STATE_URI = Pattern.compile("scm/(github|github-enterprise|bitbucket-server|bitbucket-cloud|git)/");
        public static final Pattern SCM_ORGANIZATIONS_URI = Pattern.compile("scm/(github|github-enterprise|bitbucket-server|bitbucket-cloud|git)/organizations/");

        @Override
        public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException, ServletException {

            /**
             *  If request.method and HTTP verb annotations {@link GET}, {@link POST}, {@link PUT} and {@link DELETE}
             *  do not match it skips invoking this target. If there are no such annotations present then GET as default is
             *  assumed and request is dispatched to target.
             *  Additionally, requests to organizations/orgName/scm/scmName/organizations/ and organizations/orgName/scm/scmName/
             *  have to be sent via POST, because some specific implementations of Scm.getOrganizations and Resource.getState
             *  have side effects (such as sending requests to SCM APIs). All requests that try to access child routes
             *  of organizations/orgName/scm/scmName/organizations/ must be sent via POST too. We allow POST requests
             *  for these routes by checking a requested URL against a predefined pattern. Various Stapler quirks with
             *  getter methods and child routes prevent us from using standard @POST annotations on individual routes.
             */
            if (matches(request) || postRouteMatches(request, SCM_ORGANIZATIONS_URI) || postRouteMatches(request, SCM_STATE_URI)) {
                final Object resp = target.invoke(request, response, instance, arguments);

                return new HttpResponse() {
                    @Override
                    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                        Export.doJson(req, rsp, resp);
                    }
                };
            }else{
                throw new CancelRequestHandlingException();
            }
        }

        private boolean matches(StaplerRequest request) {
            String method = request.getMethod();

            for (Annotation a : target.getAnnotations()) {
                Class<? extends Annotation> t = a.annotationType();
                InterceptorAnnotation ia = t.getAnnotation(InterceptorAnnotation.class);
                if (ia !=null && ia.value()==HttpVerbInterceptor.class) {
                    return t.getName().endsWith(method);
                }
            }

            //by default, we treat it as GET
            return method.equals( "GET" );
        }

        private boolean postRouteMatches(StaplerRequest request, Pattern pattern) {
            String method = request.getMethod();
            if (!"POST".equalsIgnoreCase(method))
                return false;

            return pattern.matcher(request.getOriginalRequestURI()).find();
        }
    }
}
