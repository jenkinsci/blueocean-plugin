package io.jenkins.blueocean.service.embedded.rest;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.console.AnnotatedLargeText;
import io.jenkins.blueocean.commons.ServiceException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.framework.io.CharSpool;
import org.kohsuke.stapler.framework.io.LineEndNormalizingWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * @author Vivek Pandey
 */
public class LogResource{
    public static final long DEFAULT_LOG_THRESHOLD = 150;

    private final AnnotatedLargeText logText;
    private final Reader appenderLogReader;

    public LogResource(AnnotatedLargeText log) {
        this(log, LogAppender.DEFAULT);
    }

    public LogResource(@NonNull AnnotatedLargeText log, @NonNull LogAppender logAppender) {
        this.logText = log;
        this.appenderLogReader = logAppender.getLog();
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp){
        writeLog(req,rsp);
    }

    private void writeLog(StaplerRequest req, StaplerResponse rsp) {
        try {
            String download = req.getParameter("download");

            if("true".equalsIgnoreCase(download)) {
                rsp.setHeader("Content-Disposition", "attachment; filename=log.txt");
            }

            rsp.setContentType("text/plain;charset=UTF-8");
            rsp.setStatus(HttpServletResponse.SC_OK);

            writeLogs(req, rsp);
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to get logText: " + e.getMessage(), e);
        }
    }

    private void writeLogs(StaplerRequest req, StaplerResponse rsp) throws IOException {
        long threshold = DEFAULT_LOG_THRESHOLD * 1024;

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

        Writer w = createWriter(req, rsp, r - offset);
        spool.writeTo(new LineEndNormalizingWriter(w));
        if(!logText.isComplete()) {
            rsp.addHeader("X-More-Data", "true");
        }else{
            int text = appenderLogReader.read();
            while(text != -1){
                w.write(text);
                r++;
                text = appenderLogReader.read();
            }
        }
        rsp.addHeader("X-Text-Size", String.valueOf(r));
        rsp.addHeader("X-Text-Delivered", String.valueOf(r - offset));
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
