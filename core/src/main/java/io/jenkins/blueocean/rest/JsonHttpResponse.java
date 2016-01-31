package io.jenkins.blueocean.rest;

import io.jenkins.blueocean.rest.guice.JsonConverter;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Vivek Pandey
 **/
public class JsonHttpResponse{

    public static HttpResponse json(final int status, final Object value) {
        return new HttpResponse() {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws
                    IOException, ServletException {
                rsp.setContentType(Operation.MEDIA_TYPE_APPLICATION_JSON);
                rsp.getWriter().print(JsonConverter.fromJava(value));
                rsp.setStatus(status);
            }
        };
    }
}
