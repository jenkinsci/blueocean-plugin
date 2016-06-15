package io.jenkins.blueocean.service.embedded.util;

import com.google.common.util.concurrent.ListenableFuture;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.concurrent.*;


/**
 * Listen for run events, filter and publish stage/branch events
 * to notify the UX that there are changes when viewing the
 * pipeline results screen.
 */
@Extension
public class StageEventRunListener extends RunListener<Run<?,?>> {


    private class StageEventPublisher implements GraphListener {

        @Override
        public void onNewHead(FlowNode flowNode) {


            System.err.println("---------------------------");
            System.err.println("---------------------------");
            System.err.println(flowNode.getDisplayName());
            System.err.println("   ");



            for (Action a : flowNode.getActions()) {
                System.err.println("\t" + a.getClass().toString());
                System.err.println("\n");
            }
            System.err.println("   ");
            System.err.println("---------------------------");
            System.err.println("---------------------------");

        }
    }

    private ExecutorService executor = new ThreadPoolExecutor(0, 5, 10L,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    @Override
    public void onStarted(final Run<?,?> run, TaskListener listener) {
        super.onStarted(run, listener);
        if (run instanceof WorkflowRun) {
            ListenableFuture<FlowExecution> promise = ((WorkflowRun) run).getExecutionPromise();
            promise.addListener(new Runnable() {
                @Override
                public void run() {
                    FlowExecution ex = null;
                    try {
                        ex = ((WorkflowRun) run).getExecutionPromise().get();
                        ex.addListener(new StageEventPublisher());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }, executor);
        }
    }




}
