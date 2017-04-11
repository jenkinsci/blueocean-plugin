package io.jenkins.blueocean.blueocean_github_pipeline;

import hudson.model.Item;
import io.jenkins.blueocean.commons.ServiceException.UnexpectedErrorException;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.util.Timer;
import org.jenkinsci.plugins.pubsub.MessageException;
import org.jenkinsci.plugins.pubsub.PubsubBus;
import org.jenkinsci.plugins.pubsub.SimpleMessage;

import java.util.concurrent.TimeUnit;

class SSEEvents {

    static void sendOrganizationScanCompleteEvent(final Item item, final OrganizationFolder orgFolder) {
        Timer.get().schedule(new MessageSender(createOrganizationScanCompleteEvent(item, orgFolder), "org scan complete"), 1, TimeUnit.SECONDS);
    }

    static void sendMultibranchIndexingCompleteEvent(final Item item, final OrganizationFolder orgFolder, final String name, final int iterations) {
        sendMultibranchIndexingCompleteEventInternal(item, orgFolder, name, iterations);
    }

    static class MessageSender implements Runnable {
        private final SimpleMessage message;
        private final String kind;

        MessageSender(SimpleMessage message, String kind) {
            this.message = message;
            this.kind = kind;
        }

        @Override
        public void run() {
            try {
                PubsubBus.getBus().publish(message);
            } catch (MessageException e) {
                throw new UnexpectedErrorException("Failed to send " + kind + " event", e);
            }
        }
    }

    static SimpleMessage createOrganizationScanCompleteEvent(Item item, OrganizationFolder orgFolder) {
        SimpleMessage msg = new SimpleMessage();
        msg.set("jenkins_object_type","jenkins.branch.OrganizationFolder");
        msg.set("job_run_status","ALLOCATED");
        msg.set("job_name",orgFolder.getName());
        msg.set("jenkins_org","jenkins");
        msg.set("job_orgfolder_indexing_status","COMPLETE");
        msg.set("job_run_queueId","1");
        msg.set("jenkins_object_name",orgFolder.getName());
        msg.set("blueocean_job_rest_url","/blue/rest/organizations/jenkins/pipelines/"+orgFolder.getName()+"/");
        msg.set("jenkins_event","job_run_queue_task_complete");
        msg.set("job_orgfolder_indexing_result","SUCCESS");
        msg.set("blueocean_job_pipeline_name",orgFolder.getName());
        msg.set("jenkins_object_url","job/"+orgFolder.getName()+"/");
        msg.set("jenkins_channel","job");
        return msg;
    }

    @SuppressWarnings({ "rawtypes" })
    static void sendMultibranchIndexingCompleteEventInternal(Item item, OrganizationFolder orgFolder, String name, int iterations) {
        MultiBranchProject mbp = orgFolder.getItem(name);
        if (mbp == null) {
            if (iterations <= 0) {
                return; // not found
            }
            sendMultibranchIndexingCompleteEvent(item, orgFolder, name, iterations - 1);
            return;
        }
        Timer.get().schedule(new MessageSender(createMultibranchIndexingCompleteEvent(orgFolder, mbp), "multibranch indexing complete"), 1, TimeUnit.SECONDS);
    }

    private static SimpleMessage createMultibranchIndexingCompleteEvent(OrganizationFolder orgFolder, MultiBranchProject mbp) {
        String jobName = orgFolder.getName() + "/" + mbp.getName();
        SimpleMessage msg = new SimpleMessage();
        msg.set("jenkins_object_type","org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject");
        msg.set("job_run_status","QUEUED");
        msg.set("job_name",jobName);
        msg.set("jenkins_org","jenkins");
        msg.set("job_run_queueId","11");
        msg.set("job_ismultibranch","true");
        msg.set("jenkins_object_name",jobName);
        msg.set("blueocean_job_rest_url","/blue/rest/organizations/jenkins/pipelines/" + orgFolder.getName() + "/pipelines/" + mbp.getName() + "/");
        msg.set("job_multibranch_indexing_status","INDEXING");
        msg.set("jenkins_event","job_run_queue_enter");
        msg.set("blueocean_job_pipeline_name",jobName);
        msg.set("jenkins_object_url","job/" + orgFolder.getName() + "/job/" + mbp.getName() + "/");
        msg.set("jenkins_channel","job");
        return msg;
    }

    private SSEEvents() {}
}
