package io.jenkins.blueocean.executor;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.queue.SubTask;
import io.jenkins.blueocean.rest.OrganizationRoute;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.factory.BlueRunFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.rest.model.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * @author kzantow
 */
@Extension
@ExportedBean
public class Computers extends Resource implements OrganizationRoute {
    @Override
    public String getUrlName() {
        return "computers";
    }

    @Override
    public Link getLink() {
        return getOrganization().getLink().rel(getUrlName());
    }
    
    private static BlueOrganization getOrganization() {
        // This should always have an organization as a parent, as it's an OrganizationRoute
        Ancestor ancestor = Stapler.getCurrentRequest().findAncestor(BlueOrganization.class);
        BlueOrganization organization = (BlueOrganization)ancestor.getObject();
        return organization;
    }
    
    @Exported(inline=true)
    public ComputerInfo[] getComputers() throws Exception {
        List<ComputerInfo> info = new ArrayList<>();
        Jenkins j = Jenkins.getInstance();
        for (Computer c : j.getComputers()) {
            info.add(new ComputerInfo(getLink(), c));
        }
        return info.toArray(new ComputerInfo[info.size()]);
    }

    @ExportedBean
    public static class ComputerInfo extends Resource {
        final Link parent;
        Computer computer;

        public ComputerInfo(Link parent, Computer computer) {
            this.parent = parent;
            this.computer = computer;
        }

        @Exported
        public String getDisplayName() {
            return computer.getDisplayName();
        }

        @Exported(inline=true)
        public Container<ExecutorInfo> getExecutors() {
            final List<ExecutorInfo> out = new ArrayList<>();
            if (computer != null) {
                for (Executor e : computer.getExecutors()) {
                    out.add(new ExecutorInfo(this, e));
                }
            }
            return new Container() {
                @Override
                public Object get(String string) {
                    throw new UnsupportedOperationException("Not supported.");
                }

                @Override
                public Iterator iterator() {
                    return out.iterator();
                }

                @Override
                public Link getLink() {
                    return parent.rel("executors");
                }
            };
        }

        @Override
        public Link getLink() {
            return parent.rel("computer");
        }
    }
    
    @ExportedBean
    public static class ExecutorInfo extends Resource {
        final Resource parent;
        final Executor executor;
        private ExecutorInfo(Resource parent, Executor executor) {
            this.parent = parent;
            this.executor = executor;
        }

        @Override
        public Link getLink() {
            return parent.getLink().rel("executor");
        }
        
        @Exported
        public String getDisplayName() {
            return executor.getDisplayName();
        }
        
        @Exported
        public boolean isIdle() {
            return executor.isIdle();
        }

        @Exported(inline=true)
        public BlueRun getRun() {
            Queue.Executable e = executor.getCurrentExecutable();
            if (e == null) {
                return null;
            }
            Run r = null;
            SubTask subTask = e.getParent();
            if (subTask instanceof ExecutorStepExecution.PlaceholderTask) {
                ExecutorStepExecution.PlaceholderTask task = (ExecutorStepExecution.PlaceholderTask)subTask;
                r = task.run();
            }
            if (e instanceof WorkflowRun) {
                r = (WorkflowRun)e;
            }
            if (e instanceof AbstractBuild) {
                r = (AbstractBuild)e;
            }
            if (r != null) {
                Item pipelineJobItem = r.getParent();
                System.out.println(pipelineJobItem.getFullDisplayName());
                BluePipeline pipeline = (BluePipeline)BluePipelineFactory.resolve(pipelineJobItem); //BluePipelineFactory.getPipelineInstance(item, getOrganization());
                System.out.println(pipeline.getFullDisplayName());
                return BlueRunFactory.getRun(r, pipeline);
            }
            return null;
        }
    }
}
