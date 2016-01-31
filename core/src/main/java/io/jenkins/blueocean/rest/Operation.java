package io.jenkins.blueocean.rest;

import com.fasterxml.jackson.databind.JsonNode;
import io.jenkins.blueocean.rest.guice.JsonConverter;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 * Operation maps an HTTP request to a Java method. Annotate your Java method with this annotation to enforce
 * HTTP method and JSON specific behavior.
 *
 * By default Stapler would send GET, POSt, PUT all http method calls to your doXXX() method for /xxx call. This
 * Does not work well for REST API. For example we want to report different http errors based on bad inputs. For example,
 * a method can only work with POST and not GET.
 *
 * <p>
 * example:
 * <pre>
 * <code>
 *
 *{@literal @}Operation(method="POST")
 * public Map doEcho(Map body, HttpParams params){
 *     return ImmutableMap.of("message", jsonNode.get("message").asText(), "params", paramMap.toString());
 * }
 *
 * curl -v -X POST -H 'Content-Type: application/json' http://localhost:8080/jenkins/echo\?name\=joe -d '{"message":"Hello World!"}'
 *
 * HTTP/1.1 200 OK
 * {
 *   "message" : "Hello World!",
 *   "params" : "{name=[joe]}"
 * }
 *
 * curl -v -X GET -H 'Content-Type: application/json' http://localhost:8080/jenkins/echo -d '{"message":"Hello World!"}'
 *
 * HTTP/1.1 405 Method Not Allowed
 * {
 *   "message" : "Method not allowed. Expected POST got GET",
 *   "errors" : [ ]
 * }
 *
 * curl -v -X POST -H 'Content-Type: text/html' http://localhost:8080/jenkins/echo -d '{"message":"Hello World!"}'
 *
 * HTTP/1.1 415 Unsupported Media Type
 * {
 *   "message" : "Unsupported media type. Content-Type: application/json;charset=UTF-8 expected.",
 *   "errors" : [ ]
 * }
 * </code>
 * </pre>
 * @author Vivek Pandey
 **/
@Retention(RUNTIME)
@Target({METHOD})
@InterceptorAnnotation(Operation.Processor.class)
public @interface Operation {
    public static final String MEDIA_TYPE_APPLICATION_JSON = "application/json;charset=UTF-8";

    /** Only this Http method can be handled, for others 405 is returned **/
    String method() default "GET";

    /** Expected Content-Type by this method **/
    String supportedContentType() default  MEDIA_TYPE_APPLICATION_JSON;

    /**
     * Incoming JSON body will be mapped to this java type. This assumes there is only one method argument,
     * if there are more than one parameter the mapping does not takes place.
     *
     * Only applies to POST, PUT and PATCH methods. For GET, there is no body. Query params should be handled using
     * {@link StaplerRequest#getParameter(String)}.
     *
     * It must be a JSON serializable class. Most basic Java types that Jackson JSON library can handle should work.
     *
     */
    Class bodyJavaType() default JsonNode.class;

    /**
     *  Interceptor to check for errors and do pre and post processing, trap errors  to return JSON error message
     */
    public static class Processor extends Interceptor {

        @Override
        public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException {

            Operation annotation = target.getAnnotation((Operation.class));

            if(!request.getMethod().toUpperCase().equals(annotation.method())){
                return JsonHttpResponse.json(405, new ErrorMessage(String.format("Method not allowed. Expected %s got %s.",
                        annotation.method(), request.getMethod())));
            }

            if(!request.getContentType().toLowerCase().startsWith("application/json")){
                return JsonHttpResponse.json(415, new ErrorMessage(
                        String.format("Unsupported media type. Content-Type: %s expected.",
                                annotation.supportedContentType())));
            }

            try {
                if(request.getMethod().equals("GET") || request.getMethod().equals("DELETE") ||
                        request.getMethod().equals("HEAD")) {
                    //We can only map json body to one object at present.
                    if (arguments.length == 1 && target.getParameterTypes()[0].isAssignableFrom(HttpParams.class)) {
                        arguments[0] = new HttpParams(request.getParameterMap());
                    }
                }else{ //POST, PUT, PATCH
                    if (arguments.length == 1) {
                        arguments[0] = JsonConverter.toJava(request.getInputStream(), annotation.bodyJavaType());
                    }else if (arguments.length == 2){ //second param must be HttpParams
                        for(int i=0; i < 2; i++){
                            if(target.getParameterTypes()[i].isAssignableFrom(HttpParams.class)){
                                arguments[i] = new HttpParams(request.getParameterMap());
                                arguments[i==0?1:0] = JsonConverter.toJava(request.getInputStream(),
                                        annotation.bodyJavaType());
                            }
                        }
                    }
                }

                Object resp = target.invoke(request, response, instance, arguments);

                if (resp instanceof HttpResponse) {
                    return resp;
                } else {
                    // If not HttpRespons and there was no exception we assume it was success and we return 200
                    return JsonHttpResponse.json(200, resp);
                }
            } catch (WebException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return JsonHttpResponse.json(e.status, e.errorMessage);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return JsonHttpResponse.json(500,
                        new ErrorMessage(String.format("Unexpected error: %s",e.getMessage())));
            }
        }
        private static final Logger LOGGER = Logger.getLogger(Operation.class.getName());
    }


}
