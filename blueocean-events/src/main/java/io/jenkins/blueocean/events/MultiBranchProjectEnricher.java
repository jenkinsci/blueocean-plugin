/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
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
import hudson.model.Queue;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.MultiBranchProject.BranchIndexing;
import org.jenkins.pubsub.EventProps;
import org.jenkins.pubsub.Events;
import org.jenkins.pubsub.JobChannelMessage;
import org.jenkins.pubsub.Message;
import org.jenkins.pubsub.MessageEnricher;
import org.jenkins.pubsub.QueueTaskMessage;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
public class MultiBranchProjectEnricher extends MessageEnricher {

    @Override
    public void enrich(@Nonnull Message message) {
        if (message instanceof JobChannelMessage) {
            JobChannelMessage jobChannelMessage = (JobChannelMessage) message;
            Item jobChannelItem = jobChannelMessage.getJobChannelItem();

            if (jobChannelItem instanceof MultiBranchProject) {
                jobChannelMessage.set(EventProps.Job.job_ismultibranch, "true");

                if (message instanceof QueueTaskMessage) {
                    QueueTaskMessage queueTaskMessage = (QueueTaskMessage) message;
                    Queue.Item queueItem = queueTaskMessage.getQueueItem();

                    if (queueItem instanceof Queue.LeftItem) {
                        Queue.LeftItem leftItem = (Queue.LeftItem) queueItem;
                        if (leftItem.isCancelled()) {
                            jobChannelMessage.set(EventProps.Job.job_multibranch_indexing_status, "COMPLETE");
                            jobChannelMessage.set(EventProps.Job.job_multibranch_indexing_result, "CANCELLED");
                        } else {
                            if (message.getEventName().equals(Events.JobChannel.job_run_queue_task_complete.name())) {
                                jobChannelMessage.set(EventProps.Job.job_multibranch_indexing_status, "COMPLETE");
                                Queue.Executable executable = ((Queue.LeftItem) queueItem).getExecutable();
                                if (executable instanceof BranchIndexing) {
                                    BranchIndexing branchIndexing = (BranchIndexing) executable;
                                    jobChannelMessage.set(EventProps.Job.job_multibranch_indexing_result, branchIndexing.getResult().toString());
                                }
                            } else {
                                jobChannelMessage.set(EventProps.Job.job_multibranch_indexing_status, "INDEXING");
                            }
                        }
                    } else {
                        jobChannelMessage.set(EventProps.Job.job_multibranch_indexing_status, "INDEXING");
                    }
                }
            }
        }
    }
}
