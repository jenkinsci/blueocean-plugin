package io.jenkins.blueocean.rest;

import io.jenkins.blueocean.commons.JsonConverter;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Vivek Pandey
 **/
public class JsonHttpResponse{
    public static final String MEDIA_TYPE_APPLICATION_JSON = "application/json;charset=UTF-8";

    public static HttpResponse json(final Object value, final int status) {
        return new HttpResponse() {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws
                    IOException, ServletException {
                rsp.setContentType(MEDIA_TYPE_APPLICATION_JSON);
                rsp.getWriter().print(JsonConverter.toJson(value));
                rsp.setStatus(status);
            }
        };
    }

    public static HttpResponse json(final Object value) {
        return new HttpResponse() {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws
                IOException, ServletException {
                rsp.setContentType(MEDIA_TYPE_APPLICATION_JSON);
                rsp.getWriter().print(JsonConverter.toJson(value));
                rsp.setStatus(200);
            }
        };
    }
}
