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
import hudson.model.Result;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.MultiBranchProject.BranchIndexing;
import jenkins.branch.OrganizationFolder;
import org.jenkinsci.plugins.pubsub.EventProps;
import org.jenkinsci.plugins.pubsub.Events;
import org.jenkinsci.plugins.pubsub.JobChannelMessage;
import org.jenkinsci.plugins.pubsub.Message;
import org.jenkinsci.plugins.pubsub.MessageEnricher;
import org.jenkinsci.plugins.pubsub.QueueTaskMessage;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * MessageEnricher that adds information when MultiBranchProject or OrganizationFolder indexing succeeds or fails.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
public class JobIndexingMessageEnricher extends MessageEnricher {

    @Override
    public void enrich(@NonNull Message message) {
        if (message instanceof JobChannelMessage) {
            JobChannelMessage jobChannelMessage = (JobChannelMessage) message;
            Item jobChannelItem = jobChannelMessage.getJobChannelItem();

            Enum indexingStatus;
            Enum indexingResult;

            if (jobChannelItem instanceof OrganizationFolder) {
                indexingStatus = JobIndexing.EventProps.job_orgfolder_indexing_status;
                indexingResult = JobIndexing.EventProps.job_orgfolder_indexing_result;
            } else if (jobChannelItem instanceof MultiBranchProject) {
                jobChannelMessage.set(EventProps.Job.job_ismultibranch, "true");
                indexingStatus = EventProps.Job.job_multibranch_indexing_status;
                indexingResult = EventProps.Job.job_multibranch_indexing_result;
            } else {
                // don't enrich if not org folder or multibranch
                return;
            }

            if (message instanceof QueueTaskMessage) {
                QueueTaskMessage queueTaskMessage = (QueueTaskMessage) message;
                Queue.Item queueItem = queueTaskMessage.getQueueItem();

                if (queueItem instanceof Queue.LeftItem) {
                    Queue.LeftItem leftItem = (Queue.LeftItem) queueItem;
                    if (leftItem.isCancelled()) {
                        jobChannelMessage.set(indexingStatus, "COMPLETE");
                        jobChannelMessage.set(indexingResult, "CANCELLED");
                    } else {
                        if (message.getEventName().equals(Events.JobChannel.job_run_queue_task_complete.name())) {
                            jobChannelMessage.set(indexingStatus, "COMPLETE");
                            Queue.Executable executable = ((Queue.LeftItem) queueItem).getExecutable();
                            if (executable instanceof BranchIndexing) {
                                BranchIndexing branchIndexing = (BranchIndexing) executable;
                                Result result = branchIndexing.getResult();
                                if(result != null) {
                                    jobChannelMessage.set(indexingResult, result.toString());
                                }
                            } else if (executable instanceof OrganizationFolder.OrganizationScan) {
                                OrganizationFolder.OrganizationScan orgScan = (OrganizationFolder.OrganizationScan) executable;
                                Result result = orgScan.getResult();
                                if(result != null) {
                                    jobChannelMessage.set(indexingResult, result.toString());
                                }
                            }
                        } else {
                            jobChannelMessage.set(indexingStatus, "INDEXING");
                        }
                    }
                } else {
                    jobChannelMessage.set(indexingStatus, "INDEXING");
                }
            }
        }
    }

    public interface JobIndexing{
        enum EventProps {
            job_orgfolder_indexing_status,
            job_orgfolder_indexing_result
        }
    }
}
