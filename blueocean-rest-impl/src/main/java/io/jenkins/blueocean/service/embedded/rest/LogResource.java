package io.jenkins.blueocean.service.embedded.rest;

import hudson.console.AnnotatedLargeText;
import io.jenkins.blueocean.commons.ServiceException;
import org.kohsuke.stapler.AcceptHeader;
import org.kohsuke.stapler.Header;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.framework.io.CharSpool;
import org.kohsuke.stapler.framework.io.LineEndNormalizingWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * @author Vivek Pandey
 */
public class LogResource{
    public static final long DEFAULT_LOG_THREASHOLD = 150;

    private final AnnotatedLargeText logText;
    public LogResource(AnnotatedLargeText log) {
        this.logText = log;
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp, @Header("Accept") AcceptHeader accept){
        writeLog(req,rsp,accept);
    }

    private void writeLog(StaplerRequest req, StaplerResponse rsp, AcceptHeader accept) {
        try {
            switch (accept.select("text/html","text/plain")) {
                case "text/html":
                    rsp.setContentType("text/html;charset=UTF-8");
                    rsp.setStatus(HttpServletResponse.SC_OK);
                    req.setAttribute("html", Boolean.valueOf(true));
                case "text/plain":
                    rsp.setContentType("text/plain;charset=UTF-8");
                    rsp.setStatus(HttpServletResponse.SC_OK);
            }
            writeLogs(req, rsp);
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to get logText: " + e.getMessage(), e);
        }
    }

    private void writeLogs(StaplerRequest req, StaplerResponse rsp) throws IOException {
        long threshold = DEFAULT_LOG_THREASHOLD * 1024;

        String s = req.getParameter("thresholdInKB");
        if(s!=null) {
            threshold = Long.parseLong(s) * 1024;
        }
        long offset;
        if(req.getParameter("start") != null){
            offset = Long.parseLong(req.getParameter("start"));
        }else if(logText.length() > threshold){
            offset = logText.length()-threshold;
        } else{
            offset = 0;
        }

        CharSpool spool = new CharSpool();

        long r = logText.writeLogTo(offset,spool);
        rsp.addHeader("X-Text-Size",String.valueOf(r));
        if(!logText.isComplete()) {
            rsp.addHeader("X-More-Data", "true");
        }

        Writer w = createWriter(req, rsp, r - offset);
        spool.writeTo(new LineEndNormalizingWriter(w));
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
