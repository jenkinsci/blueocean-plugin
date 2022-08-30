/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.blueocean.events;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Queue;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineImpl;
import io.jenkins.blueocean.service.embedded.rest.QueueUtil;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.pubsub.EventProps;
import org.jenkinsci.plugins.pubsub.Events;
import org.jenkinsci.plugins.pubsub.JobChannelMessage;
import org.jenkinsci.plugins.pubsub.Message;
import org.jenkinsci.plugins.pubsub.MessageEnricher;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
public class BlueMessageEnricher extends MessageEnricher {

    private static final Logger LOGGER = Logger.getLogger(BlueMessageEnricher.class.getName());

    enum BlueEventProps {
        blueocean_job_rest_url,
        blueocean_job_pipeline_name,
        blueocean_job_branch_name,
        blueocean_queue_item_expected_build_number
    }

    @Override
    public void enrich(@NonNull Message message) {
        try {
            maybeEnrichMessage(message);
        } catch (Exception e) {
            LOGGER.log(WARNING, "Unable to enrich message: " + e.getMessage(), e);
        }
    }

    private void maybeEnrichMessage(@NonNull Message message) {
        String channelName = message.getChannelName();
        if (channelName.equals(Events.JobChannel.NAME) && message instanceof JobChannelMessage) {
            JobChannelMessage jobChannelMessage = (JobChannelMessage) message;
            Item jobChannelItem = jobChannelMessage.getJobChannelItem();
            if(jobChannelItem == null){
                return;
            }

            Link jobUrl = LinkResolver.resolveLink(jobChannelItem);
            if (jobUrl == null) {
                return;
            }

            BlueOrganization org = OrganizationFactory.getInstance().getContainingOrg(jobChannelItem);
            if (org!=null) {
                message.set(EventProps.Jenkins.jenkins_org, org.getName());
            }
            jobChannelMessage.set(BlueEventProps.blueocean_job_rest_url, jobUrl.getHref());
            jobChannelMessage.set(BlueEventProps.blueocean_job_pipeline_name, AbstractPipelineImpl.getFullName(org, jobChannelItem));
            if (jobChannelItem instanceof WorkflowJob) {
                ItemGroup<? extends Item> parent = jobChannelItem.getParent();
                if (parent instanceof WorkflowMultiBranchProject) {
                    String multiBranchProjectName = AbstractPipelineImpl.getFullName(org, (WorkflowMultiBranchProject)parent);
                    jobChannelMessage.set(BlueEventProps.blueocean_job_pipeline_name, multiBranchProjectName);
                    jobChannelMessage.set(BlueEventProps.blueocean_job_branch_name, jobChannelItem.getName());
                }
            }

            if (message.containsKey("job_run_queueId") && jobChannelItem instanceof hudson.model.Job) {
                String queueIdStr = message.get("job_run_queueId");
                if (queueIdStr == null) {
                    return;
                }
                final long queueId = Long.parseLong(queueIdStr);
                Queue.Item queueItem = Jenkins.get().getQueue().getItem(queueId);
                if (queueItem == null) {
                    return;
                }
                hudson.model.Job job = (hudson.model.Job) jobChannelItem;
                BlueQueueItem blueQueueItem = QueueUtil.getQueuedItem(null, queueItem, job);
                if (blueQueueItem != null) {
                    jobChannelMessage.set(BlueEventProps.blueocean_queue_item_expected_build_number, Integer.toString(blueQueueItem.getExpectedBuildNumber()));
                } else {
                    // If build is already running, we simply take the run id and pass it on
                    if(message.get("job_run_status") != null){
                        String buildNumberStr = message.get("jenkins_object_id");
                        if(StringUtils.isNotBlank(buildNumberStr)) {
                            jobChannelMessage.set(BlueEventProps.blueocean_queue_item_expected_build_number, message.get("jenkins_object_id"));
                        }
                    }
                }
            }

        }
    }
}
