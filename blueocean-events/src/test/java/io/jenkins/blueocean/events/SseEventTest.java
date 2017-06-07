package io.jenkins.blueocean.events;

import hudson.ExtensionList;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import hudson.model.queue.QueueTaskFuture;
import hudson.util.OneShotEvent;
import io.jenkins.blueocean.events.sse.SSEConnection;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.service.embedded.OrganizationFactoryImpl;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.pubsub.ChannelSubscriber;
import org.jenkinsci.plugins.pubsub.Message;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStepExecution;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestExtension;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static io.jenkins.blueocean.events.BlueMessageEnricher.BlueEventProps.*;
import static io.jenkins.blueocean.events.JobIndexingMessageEnricher.JobIndexing.EventProps.job_orgfolder_indexing_result;
import static io.jenkins.blueocean.events.JobIndexingMessageEnricher.JobIndexing.EventProps.job_orgfolder_indexing_status;
import static io.jenkins.blueocean.events.PipelineEventChannel.EventProps.*;
import static org.jenkinsci.plugins.pubsub.EventProps.Jenkins.*;
import static org.jenkinsci.plugins.pubsub.EventProps.Job;
import static org.jenkinsci.plugins.pubsub.EventProps.Job.*;
import static org.junit.Assert.*;

/**
 * @author Vivek Pandey
 */
public class SseEventTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Test
    public void jobCreationEvents() throws IOException, ExecutionException, InterruptedException {
        final OneShotEvent success = new OneShotEvent();

        SSEConnection con = new SSEConnection(j.getURL(), "me", new ChannelSubscriber() {
            @Override
            public void onMessage(@Nonnull Message message) {
                System.out.println(message);
                assertEquals("job_crud_created", message.get(jenkins_event));
                assertEquals("/blue/rest/organizations/jenkins/pipelines/test1/", message.get(blueocean_job_rest_url));
                assertEquals("test1", message.get(blueocean_job_pipeline_name));
                assertEquals("test1", message.get(job_name));
                assertEquals("job", message.get(jenkins_channel));
                assertNull(message.get(job_ismultibranch));
                assertNull(message.get(job_multibranch_indexing_result));
                assertNull(message.get(job_multibranch_indexing_status));
                assertNull(message.get(Job.job_run_queueId));
                assertNull(message.get(Job.job_run_status));

                if ("job_crud_created".equals(message.get(jenkins_event)))
                    success.signal();
            }
        });

        con.subscribe("job");

        j.createFreeStyleProject("test1");

        // make sure we got the event we were looking for
        success.block(5000);
        con.close();
    }


    @Test
    public void jobRunEvents() throws IOException, ExecutionException, InterruptedException {
        final OneShotEvent success = new OneShotEvent();

        final FreeStyleProject p = j.createFreeStyleProject("test1");

        SSEConnection con = new SSEConnection(j.getURL(), "me", new ChannelSubscriber() {
            @Override
            public void onMessage(@Nonnull Message message) {
                System.out.println(message);
                assertEquals("/blue/rest/organizations/jenkins/pipelines/test1/", message.get(blueocean_job_rest_url));
                assertEquals(p.getName(), message.get(blueocean_job_pipeline_name));
                assertEquals("1", message.get(blueocean_queue_item_expected_build_number));
                assertEquals(p.getName(), message.get(job_name));
                assertEquals("job", message.get(jenkins_channel));
                assertEquals("jenkins", message.get(jenkins_org));
                assertNull(message.get(job_ismultibranch));
                assertNull(message.get(job_multibranch_indexing_result));
                assertNull(message.get(job_multibranch_indexing_status));
                assertNotNull(message.get(Job.job_run_queueId));
                assertNotNull(message.get(Job.job_run_status));

                if ("SUCCESS".equals(message.get(Job.job_run_status))
                        && "job_run_ended".equals(message.get(jenkins_event)))
                    success.signal();
            }
        });
        con.subscribe("job");
        p.scheduleBuild2(0).get();

        // make sure we got the event we were looking for
        success.block(5000);
        con.close();
    }

    @Test
    public void pipelineWithInput() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        final OneShotEvent success = new OneShotEvent();

        String script = "node {\n" +
                "  stage(\"build\"){\n" +
                "    echo \"running\"\n" +
                "    input message: 'Please input branch to test against', parameters: [[$class: 'StringParameterDefinition', defaultValue: 'master', description: '', name: 'branch']]\n" +
                "  }\n" +
                "}";

        final boolean[] wasPaused = {false};
        final boolean[] wasUnPaused = {false};
        SSEConnection con = new SSEConnection(j.getURL(), "me", new ChannelSubscriber() {
            @Override
            public void onMessage(@Nonnull Message message) {
                System.out.println(message);
                if("job".equals(message.get(jenkins_channel))) {
                    assertEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/", message.get(blueocean_job_rest_url));
                    assertEquals("pipeline1", message.get(blueocean_job_pipeline_name));
                    assertEquals("1", message.get(blueocean_queue_item_expected_build_number));
                    assertEquals("pipeline1", message.get(job_name));
                    assertEquals("job", message.get(jenkins_channel));
                    assertEquals("jenkins", message.get(jenkins_org));
                    assertNull(message.get(job_ismultibranch));
                    assertNull(message.get(job_multibranch_indexing_result));
                    assertNull(message.get(job_multibranch_indexing_status));
                    assertNotNull(message.get(Job.job_run_queueId));
                    assertNotNull(message.get(Job.job_run_status));
                    if("job_run_unpaused".equals(message.get(jenkins_event))){
                        wasUnPaused[0] = true;
                    }
                }else if("pipeline".equals(message.get(jenkins_channel))){
                    assertEquals("1", message.get(pipeline_run_id));
                    assertEquals("build", message.get(pipeline_step_stage_name));
                    if("input".equals(message.get(pipeline_step_name))){
                        wasPaused[0] = true;
                        assertEquals("true", message.get(pipeline_step_is_paused));
                    }
                }
                if(wasPaused[0] && wasUnPaused[0]){ // signal finish only when both conditions are met
                    success.signal();
                }
            }
        });

        con.subscribe("pipeline");
        con.subscribe("job");

        WorkflowJob job1 = j.jenkins.createProject(WorkflowJob.class, "pipeline1");
        job1.setDefinition(new CpsFlowDefinition(script, false));
        QueueTaskFuture<WorkflowRun> buildTask = job1.scheduleBuild2(0);
        WorkflowRun run = buildTask.getStartCondition().get();
        CpsFlowExecution e = (CpsFlowExecution) run.getExecutionPromise().get();

        while (run.getAction(InputAction.class) == null) {
            e.waitForSuspension();
        }

        //Now that flow is paused, send a signal that it's un-paused
        ExtensionList<PipelineEventListener.InputStepPublisher> inputStepPublisherList =
                ExtensionList.lookup(PipelineEventListener.InputStepPublisher.class);
        assertFalse(inputStepPublisherList.isEmpty());

        InputAction inputAction = run.getAction(InputAction.class);
        List<InputStepExecution> executionList = inputAction.getExecutions();
        assertFalse(executionList.isEmpty());
        InputStep inputStep = executionList.get(0).getInput();
        inputStepPublisherList.get(0).onStepContinue(inputStep,run);

        success.block(5000);
        con.close();
        if(success.isSignaled()){
            assertTrue(wasPaused[0]);
            assertTrue(wasUnPaused[0]);
        }
    }

    @Test
    public void multiBranchJobEvents() throws Exception {
        setupScm();

        final OneShotEvent success = new OneShotEvent();

        //index: 0 QUEUED, index 1: ALLOCATED, index: 2 INDEXING, index: 3 COMPLETED
        final boolean[] mbpStatus = {false, false, false, false};

        //index: 0 created, index: 1 QUEUED, index 2: ALLOCATED, index: 3 RUNNING, index: 4 SUCCESS
        final boolean[] masterBranchStatus = {false, false, false, false, false};
        final boolean[] feature1BranchStatus = {false, false, false, false, false};

        final boolean[] masterBranchPipelineEvent = {false};
        final boolean[] feature1BranchPipelineEvent = {false};

        SSEConnection con = new SSEConnection(j.getURL(), "me", new ChannelSubscriber() {
            @Override
            public void onMessage(@Nonnull Message message) {
                System.out.println(message);
                if("job".equals(message.get(jenkins_channel))) {
                    if ("org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject".equals(message.get(jenkins_object_type))) {
                        assertEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/", message.get(blueocean_job_rest_url));
                        assertEquals("pipeline1", message.get(blueocean_job_pipeline_name));
                        assertEquals("job", message.get(jenkins_channel));
                        assertEquals("jenkins", message.get(jenkins_org));
                        assertEquals("true", message.get(job_ismultibranch));
                        assertEquals("pipeline1", message.get(job_name));
                        assertNull(message.get(job_orgfolder_indexing_status));
                        assertNull(message.get(job_orgfolder_indexing_result));
                        if ("QUEUED".equals(message.get(job_run_status))) {
                            mbpStatus[0] = true; // queued
                        }
                        if ("ALLOCATED".equals(message.get(job_run_status))) {
                            mbpStatus[1] = true; // allocated or left queue
                        }
                        if ("INDEXING".equals(message.get(job_multibranch_indexing_status))) {
                            mbpStatus[2] = true; // indexing started
                            assertEquals("1", message.get(job_run_queueId));
                        }

                        if ("SUCCESS".equals(message.get(job_multibranch_indexing_result)) ||
                                ("COMPLETED".equals(message.get(job_multibranch_indexing_status))
                                        && "job_run_queue_task_complete".equals(message.get(jenkins_event)))
                                ) {
                            mbpStatus[3] = true; // indexing completed
                        }
                    } else if ("org.jenkinsci.plugins.workflow.job.WorkflowJob".equals(message.get(jenkins_object_type))) {
                        assertEquals("pipeline1", message.get(blueocean_job_pipeline_name));
                        if ("pipeline1/master".equals(message.get(job_name))) {
                            System.out.println("job_run_status::::: " + message.get(job_run_status));
                            assertEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/branches/master/", message.get(blueocean_job_rest_url));
                            assertEquals("master", message.get(blueocean_job_branch_name));
                            if ("job_crud_created".equals(message.get(jenkins_event))) {
                                masterBranchStatus[0] = true;
                            }
                            if ("QUEUED".equals(message.get(job_run_status))) {
                                masterBranchStatus[1] = true;
                            } else if ("ALLOCATED".equals(message.get(job_run_status))) {
                                masterBranchStatus[2] = true;
                            } else if ("RUNNING".equals(message.get(job_run_status))) {
                                System.out.println("in master running.....");
                                assertEquals("1", message.get(blueocean_queue_item_expected_build_number));
                                masterBranchStatus[3] = true;
                            }

                            if ("SUCCESS".equals(message.get(job_run_status))
                                    || "job_run_queue_task_complete".equals(message.get(jenkins_event))) {
                                masterBranchStatus[4] = true;
                            }
                        } else if ("pipeline1/feature%2Fux-1".equals(message.get(job_name))) {
                            assertEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/branches/feature%252Fux-1/", message.get(blueocean_job_rest_url));
                            assertEquals("feature%2Fux-1", message.get(blueocean_job_branch_name));
                            if ("job_crud_created".equals(message.get(jenkins_event))) {
                                feature1BranchStatus[0] = true;
                            }
                            System.out.println("job_run_status::::: " + message.get(job_run_status));
                            if ("QUEUED".equals(message.get(job_run_status))) {
                                feature1BranchStatus[1] = true;
                            } else if ("ALLOCATED".equals(message.get(job_run_status))) {
                                feature1BranchStatus[2] = true;
                            }

                            if ("SUCCESS".equals(message.get(job_run_status))
                                    || "job_run_queue_task_complete".equals(message.get(jenkins_event))) {
                                feature1BranchStatus[4] = true;
                            }
                        }
                    } else if ("org.jenkinsci.plugins.workflow.job.WorkflowRun".equals(message.get(jenkins_object_type))) {
                        if ("pipeline1/master".equals(message.get(job_name))) {
                            if ("RUNNING".equals(message.get(job_run_status))) {
                                assertEquals("1", message.get(blueocean_queue_item_expected_build_number));
                                masterBranchStatus[3] = true;
                            }
                        } else if ("pipeline1/feature%2Fux-1".equals(message.get(job_name))) {
                            if ("RUNNING".equals(message.get(job_run_status))) {
                                assertEquals("1", message.get(blueocean_queue_item_expected_build_number));
                                feature1BranchStatus[3] = true;
                            }
                        }
                    }
                    //all completed
                    if (mbpStatus[3] && masterBranchStatus[4] && feature1BranchStatus[4]) {
                        success.signal();
                    }
                }else if("pipeline".equals(message.get(jenkins_channel))){
                    if("pipeline1/master".equals(message.get(pipeline_job_name))){
                        assertEquals("1", message.get(pipeline_run_id));
                        masterBranchPipelineEvent[0]=true;
                    } else if("pipeline1/feature%2Fux-1".equals(message.get(pipeline_job_name))){
                        feature1BranchPipelineEvent[0]=true;
                        assertEquals("1", message.get(pipeline_run_id));
                    }
                    if("pipeline_stage".equals(message.get(jenkins_event))){
                        assertNotNull(message.get(pipeline_step_stage_name));
                        assertEquals("build", message.get(pipeline_step_stage_name));
                        assertNotNull(message.get(pipeline_step_flownode_id));
                        assertEquals("false", message.get(pipeline_step_is_paused));
                    }else if("pipeline_step".equals(message.get(jenkins_event))){
                        assertNotNull(message.get(pipeline_step_flownode_id));
                        assertEquals("false", message.get(pipeline_step_is_paused));
                    }
                }
            }
        });
        con.subscribe("pipeline");
        con.subscribe("job");

        final WorkflowMultiBranchProject mp = j.jenkins.createProject(WorkflowMultiBranchProject.class, "pipeline1");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false),
                new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }

        mp.scheduleBuild2(0).getFuture().get();

        WorkflowJob p = mp.getItem("master");
        if (p == null) {
            mp.getIndexing().writeWholeLogTo(System.out);
            fail("master project not found");
        }
        j.waitUntilNoActivity();
        WorkflowRun b1 = p.getLastBuild();
        assertEquals(1, b1.getNumber());
        assertEquals(2, mp.getItems().size());

        success.block(5000);
        con.close();
        if(success.isSignaled()) {
            assertTrue(masterBranchPipelineEvent[0]);
            assertTrue(feature1BranchPipelineEvent[0]);
            assertArrayEquals(mbpStatus, new boolean[]{true, true, true, true});
            assertArrayEquals(masterBranchStatus, new boolean[]{true, true, true, true, true});
            assertArrayEquals(feature1BranchStatus, new boolean[]{true, true, true, true, true});
        }
    }

    @Test
    public void multiBranchJobEventsWithCustomOrg() throws Exception {
        MockFolder folder = j.createFolder("TestOrgFolderName");
        assertNotNull(folder);

        setupScm();

        final OneShotEvent success = new OneShotEvent();

        final Boolean[] pipelineNameMatched = {null};
        final Boolean[] mbpPipelineNameMatched = {null};

        SSEConnection con = new SSEConnection(j.getURL(), "me", new ChannelSubscriber() {
            @Override
            public void onMessage(@Nonnull Message message) {
                System.out.println(message);
                if("job".equals(message.get(jenkins_channel))) {
                    if ("org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject".equals(message.get(jenkins_object_type))) {
                        if("pipeline1".equals(message.get(blueocean_job_pipeline_name))) {
                            mbpPipelineNameMatched[0] = true;
                        }else{
                            mbpPipelineNameMatched[0] = false;
                        }
                    } else if ("org.jenkinsci.plugins.workflow.job.WorkflowJob".equals(message.get(jenkins_object_type))) {
                        if("pipeline1".equals(message.get(blueocean_job_pipeline_name))) {
                            pipelineNameMatched[0] = true;
                        }else {
                            pipelineNameMatched[0] = false;
                        }
                    }
                }
                if(pipelineNameMatched[0] != null && mbpPipelineNameMatched[0] != null){
                    success.signal();
                }
            }
        });
        con.subscribe("pipeline");
        con.subscribe("job");

        final WorkflowMultiBranchProject mp = folder.createProject(WorkflowMultiBranchProject.class, "pipeline1");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false),
                new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }

        mp.scheduleBuild2(0).getFuture().get();

        WorkflowJob p = mp.getItem("master");
        if (p == null) {
            mp.getIndexing().writeWholeLogTo(System.out);
            fail("master project not found");
        }
        j.waitUntilNoActivity();
        WorkflowRun b1 = p.getLastBuild();
        assertEquals(1, b1.getNumber());
        assertEquals(2, mp.getItems().size());

        success.block(5000);
        con.close();
        if(success.isSignaled()) {
            assertTrue(pipelineNameMatched[0]);
            assertTrue(mbpPipelineNameMatched[0]);
        }
    }


    private void setupScm() throws Exception {
        // create git repo
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "node{\n" +
                "  stage('build'){\n" +
                "    echo 'Building'\n" +
                "  }  \n" +
                "}\n"        );
        sampleRepo.write("file", "initial content");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");

        //create feature branch
        sampleRepo.git("checkout", "-b", "feature/ux-1");
        sampleRepo.write("Jenkinsfile", "node{\n" +
                "  stage('build'){\n" +
                "    echo 'Building'\n" +
                "  }  \n" +
                "}\n");
        ScriptApproval.get().approveSignature("method java.lang.String toUpperCase");
        sampleRepo.write("file", "subsequent content1");
        sampleRepo.git("commit", "--all", "--message=tweaked1");
    }


    @TestExtension(value = "multiBranchJobEventsWithCustomOrg")
    public static class TestOrganizationFactoryImpl extends OrganizationFactoryImpl {
        private OrganizationImpl instance = new OrganizationImpl("TestOrg", Jenkins.getInstance().getItem("/TestOrgFolderName", Jenkins.getInstance(), MockFolder.class));

        @Override
        public OrganizationImpl get(String name) {
            if (instance != null) {
                if (instance.getName().equals(name)) {
                    System.out.println("" + name + " Intance returned " + instance);
                    return instance;
                }
            }
            System.out.println("" + name + " no instance found");
            return null;
        }

        @Override
        public Collection<BlueOrganization> list() {
            return Collections.singleton((BlueOrganization) instance);
        }

        @Override
        public OrganizationImpl of(ItemGroup group) {
            if (group == instance.getGroup() || group == Jenkins.getInstance()) {
                return instance;
            }
            return null;
        }
    }




}
