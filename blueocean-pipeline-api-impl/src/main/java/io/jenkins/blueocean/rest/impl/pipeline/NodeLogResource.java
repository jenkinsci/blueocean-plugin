package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.io.CountingOutputStream;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.kohsuke.stapler.AcceptHeader;
import org.kohsuke.stapler.Header;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.framework.io.CharSpool;
import org.kohsuke.stapler.framework.io.LineEndNormalizingWriter;
import org.kohsuke.stapler.framework.io.WriterOutputStream;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Serves logs of steps inside a node in BlueOcean DAG.
 *
 * @author Vivek Pandey
 */
public class NodeLogResource {
    private final Iterable<BluePipelineStep> steps;
    private final FlowNodeWrapper node;

    NodeLogResource(PipelineNodeImpl node) {
        this.node = node.getFlowNodeWrapper();
        this.steps = node.getSteps();
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp, @Header("Accept") AcceptHeader accept) {
        String download = req.getParameter("download");

        if("true".equalsIgnoreCase(download)) {
            rsp.setHeader("Content-Disposition", "attachment; filename=log.txt");
        }

        rsp.setContentType("text/plain;charset=UTF-8");
        rsp.setStatus(HttpServletResponse.SC_OK);

        long count = 0;
        try(CharSpool spool = new CharSpool()) {
            for (BluePipelineStep blueStep : steps) {
                if (blueStep instanceof PipelineStepImpl) {
                    PipelineStepImpl step = (PipelineStepImpl) blueStep;

                    final FlowNodeWrapper node = step.getFlowNodeWrapper();
                    if (step.getFlowNodeWrapper().isLoggable()) {
                        count += node.getNode().getAction(LogAction.class).getLogText().writeLogTo(0, spool);
                        String errorLog = node.blockError();
                        if (errorLog != null) {
                            count += appendError(errorLog, new WriterOutputStream(spool));
                        }

                    } else {
                        String errorLog = step.getFlowNodeWrapper().nodeError();
                        if (errorLog == null) {
                            errorLog = step.getFlowNodeWrapper().blockError();
                        }
                        if (errorLog != null) {
                            count += appendError(errorLog, new WriterOutputStream(spool));
                        }
                    }
                }
            }
            Writer writer;
            if (count > 0) {
                writer = (count > 4096) ? rsp.getCompressedWriter(req) : rsp.getWriter();
                spool.flush();
                spool.writeTo(new LineEndNormalizingWriter(writer));
                rsp.addHeader("X-Text-Size",String.valueOf(count));
                writer.close();
            }
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Error reading log");
        }
    }

    private long appendError(String msg, OutputStream w) throws IOException {
        try (CountingOutputStream os = new CountingOutputStream(w)) {
            os.write(msg.getBytes("UTF-8"));
            os.flush();
            return os.getCount();
        }
    }
}
