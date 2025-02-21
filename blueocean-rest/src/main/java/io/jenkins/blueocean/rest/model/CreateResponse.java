package io.jenkins.blueocean.rest.model;


import io.jenkins.blueocean.commons.stapler.Export;
import io.jenkins.blueocean.rest.Reachable;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

import jakarta.servlet.ServletException;
import java.io.IOException;

/**
 * Create response that sets Location header of the created entity and generates a tree response.
 *
 * @author Vivek Pandey
 */
public class CreateResponse implements HttpResponse {

    private final Reachable payload;

    /**
     * Constructor that takes a reachable exported bean.
     *
     * @param payload must be a {@link Reachable} and an {@link org.kohsuke.stapler.export.ExportedBean}
     */
    public CreateResponse(Reachable payload) {
        this.payload = payload;
    }

    @Override
    public void generateResponse(StaplerRequest2 req, StaplerResponse2 rsp, Object node) throws IOException, ServletException {
        rsp.setStatus(201);
        String location = String.format("%s%s", req.getRootPath(), payload.getLink());
        rsp.addHeader("Location", location);

        // Writes payload as a tree response
        Export.doJson(req, rsp, payload);
    }
}
