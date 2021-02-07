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
        @Override
        public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException, ServletException {

            /**
             *  If request.method and HTTP verb annotations {@link GET}, {@link POST}, {@link PUT} and {@link DELETE}
             *  do not match it skips invoking this target. If there no such annotations present then GET as default is
             *  assumed and request is dispatched to target.
             */
            if (matches(request)) {
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

            //by default we treat it as GET
            if(method.equals("GET")){
                return true;
            }
            return false;
        }
    }
}
