package io.jenkins.blueocean.service.embedded.rest;

import hudson.console.AnnotatedLargeText;
import io.jenkins.blueocean.commons.ServiceException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.framework.io.CharSpool;
import org.kohsuke.stapler.framework.io.LineEndNormalizingWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class LogResource{
    private final AnnotatedLargeText[] logs;

    public LogResource(AnnotatedLargeText... logs) {
        this.logs = logs;
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp){
        writeLog(req,rsp);
    }

    private void writeLog(StaplerRequest req, StaplerResponse rsp) {
        try {
            //TODO: Do better handling of accept header when stapler with AcceptHeader functionality is available
            String accept = req.getHeader("Accept");
            if (accept != null && accept.startsWith("text/html")) {
                rsp.setContentType("text/html;charset=UTF-8");
                rsp.setStatus(HttpServletResponse.SC_OK);
                req.setAttribute("html", Boolean.valueOf(true));
            } else {
                rsp.setContentType("text/plain;charset=UTF-8");
                rsp.setStatus(HttpServletResponse.SC_OK);
            }
            writeLogs(req, rsp);
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to get log: " + e.getMessage(), e);
        }
    }

    private void writeLogs(StaplerRequest req, StaplerResponse rsp) throws IOException {
        List<CharSpool> charSpools = new ArrayList<>();
        long start = 0;
        String s = req.getParameter("start");
        if(s!=null)
            start = Long.parseLong(s);
        long r = 0;
        //allCompleted to be true if all logs are complete
        boolean allCompleted = false;
        for(AnnotatedLargeText logText: logs){
            CharSpool spool = new CharSpool();
            r += logText.writeLogTo(start,spool);
            charSpools.add(spool);
            allCompleted = logText.isComplete();
        }

        Writer w = createWriter(req, rsp, r - start);
        for(CharSpool spool:charSpools){
            spool.writeTo(new LineEndNormalizingWriter(w));
        }
        rsp.addHeader("X-Text-Size",String.valueOf(r));
        if(!allCompleted) {
            rsp.addHeader("X-More-Data", "true");
        }
        w.close();
    }

    private Writer createWriter(StaplerRequest req, StaplerResponse rsp, long size) throws IOException {
        // when sending big text, try compression. don't bother if it's small
        if(size >4096)
            return rsp.getCompressedWriter(req);
        else
            return rsp.getWriter();
    }

}
