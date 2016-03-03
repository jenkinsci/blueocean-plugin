package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;
import io.jenkins.blueocean.commons.ServiceException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public class LogResource{
    private final Run run;

    public LogResource(Run run) {
        this.run = run;
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp){
        try {
            //TODO: Do better handling of accept header when stapler with AcceptHeader functionality is available
            String accept = req.getHeader("Accept");
            if(accept != null && accept.startsWith("text/html")){
                run.getLogText().doProgressiveHtml(req, rsp);
            }else {
                run.getLogText().doProgressiveText(req, rsp);
            }
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorExpcetion("Failed to get log: "+e.getMessage(), e);
        }
    }
}
