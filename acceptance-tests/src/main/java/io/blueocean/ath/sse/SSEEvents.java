package io.blueocean.ath.sse;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.List;


public class SSEEvents {
    private static Logger logger = Logger.getLogger(SSEEvents.class);
    //{"jenkins_event":"job_run_ended","jenkins_event_timestamp":"1495786661470","jenkins_event_uuid":"25238012-83ba-4430-b0a2-9711c13d2290","jenkins_object_url":"job/CommitMessagesTest_tested/job/master/1/","jenkins_object_name":"#1","jenkins_channel":"job","blueocean_job_pipeline_name":"CommitMessagesTest_tested","sse_subs_dispatcher_inst":"1377890627","sse_subs_dispatcher":"ath","jenkins_org":"jenkins","blueocean_queue_item_expected_build_number":"1","job_run_queueId":"26","jenkins_object_id":"1","jenkins_object_type":"org.jenkinsci.plugins.workflow.job.WorkflowRun","job_name":"CommitMessagesTest_tested/master","job_run_status":"SUCCESS","blueocean_job_rest_url":"/blue/rest/organizations/jenkins/pipelines/CommitMessagesTest_tested/branches/master/","blueocean_job_branch_name":"master"}
    //{"jenkins_event_timestamp":"1495787113036","jenkins_event":"job_run_queue_enter","jenkins_event_uuid":"3401562f-cf8f-4ae5-9c53-86e75e0b754f","jenkins_object_name":"CommitMessagesTest_tested","jenkins_object_url":"job/CommitMessagesTest_tested/","blueocean_job_pipeline_name":"CommitMessagesTest_tested","jenkins_channel":"job","job_multibranch_indexing_status":"INDEXING","sse_subs_dispatcher_inst":"1432227133","sse_subs_dispatcher":"ath","jenkins_org":"jenkins","job_ismultibranch":"true","job_run_queueId":"28","jenkins_object_type":"org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject","job_name":"CommitMessagesTest_tested","job_run_status":"QUEUED","blueocean_job_rest_url":"/blue/rest/organizations/jenkins/pipelines/CommitMessagesTest_tested/"}

    public static Predicate<List<JSONObject>> activityComplete(String jobName){
        return list -> {
            List<JSONObject> jobsQueued = Lists.newArrayList();
            List<JSONObject> jobsFinished = Lists.newArrayList();
            for (JSONObject json : list) {
                if(json.has("jenkins_event") && json.getString("jenkins_event").equals("job_run_queue_enter")) {
                    if(json.has("jenkins_object_type") &&
                        json.getString("jenkins_object_type").equals("org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject")) {
                        continue;
                    }
                    if(json.has("blueocean_job_pipeline_name") && json.getString("blueocean_job_pipeline_name").equals(jobName)) {
                        jobsQueued.add(json);
                    }
                }
                if(json.has("jenkins_event") && json.getString("jenkins_event").equals("job_run_ended")) {
                    if(json.has("blueocean_job_pipeline_name") && json.getString("blueocean_job_pipeline_name").equals(jobName)) {
                        jobsFinished.add(json);
                    }
                }
            }

            if(jobsQueued.size() == 0) {
                return false;
            }
            boolean finished = true;

            for (JSONObject jsonObject : jobsQueued) {

                Optional<JSONObject> found = Iterables.tryFind(jobsFinished, json -> json.getString("job_run_queueId").equals(jsonObject.getString("job_run_queueId")));
                if(!found.isPresent()) {
                    logger.info("Waiting for '" + jsonObject.getString("job_name") + "' - queueID:" + jsonObject.getString("job_run_queueId") + " to finish");
                } else {
                    logger.info("Build '" + found.get().getString("job_name") + "' - #" + found.get().getString("blueocean_queue_item_expected_build_number") + " is finished");
                }
                if(finished && !found.isPresent()) {
                    finished = false;
                }
            }


            return finished;
        };
    }
}
