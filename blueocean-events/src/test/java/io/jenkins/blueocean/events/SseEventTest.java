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
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.pubsub.ChannelSubscriber;
import org.jenkinsci.plugins.pubsub.Events;
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
import static io.jenkins.blueocean.events.BlueMessageEnricher.BlueEventProps.*;
import static io.jenkins.blueocean.events.JobIndexingMessageEnricher.JobIndexing.EventProps.job_orgfolder_indexing_result;
import static io.jenkins.blueocean.events.JobIndexingMessageEnricher.JobIndexing.EventProps.job_orgfolder_indexing_status;
import static io.jenkins.blueocean.events.PipelineEventChannel.Event.pipeline_stage;
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

        final AssertionHelper assertionHelper = new AssertionHelper();
        SSEConnection con = new SSEConnection( j.getURL(), "me", message -> {
            System.out.println(message);
            assertionHelper.isEquals("job_crud_created", message.get(jenkins_event));
            assertionHelper.isEquals("/blue/rest/organizations/jenkins/pipelines/test1/", message.get(blueocean_job_rest_url));
            assertionHelper.isEquals("test1", message.get(blueocean_job_pipeline_name));
            assertionHelper.isEquals("test1", message.get(job_name));
            assertionHelper.isEquals("job", message.get(jenkins_channel));
            assertionHelper.isNull(message.get(job_ismultibranch));
            assertionHelper.isNull(message.get(job_multibranch_indexing_result));
            assertionHelper.isNull(message.get(job_multibranch_indexing_status));
            assertionHelper.isNull(message.get(Job.job_run_queueId));
            assertionHelper.isNull(message.get(Job.job_run_status));

            if ("job_crud_created".equals(message.get(jenkins_event)))
                success.signal();
        } );

        con.subscribe("job");

        j.createFreeStyleProject("test1");

        // make sure we got the event we were looking for
        success.block(5000);
        con.close();
        if(assertionHelper.totalErrors() > 0){
            fail("There were errors: "+ assertionHelper.totalErrors());
        }
    }


    @Test
    public void jobRunEvents() throws IOException, ExecutionException, InterruptedException {
        final OneShotEvent success = new OneShotEvent();

        final FreeStyleProject p = j.createFreeStyleProject("test1");
        final AssertionHelper assertionHelper = new AssertionHelper();
        SSEConnection con = new SSEConnection( j.getURL(), "me", message -> {
            System.out.println(message);
            assertionHelper.isEquals("/blue/rest/organizations/jenkins/pipelines/test1/", message.get(blueocean_job_rest_url));
            assertionHelper.isEquals(p.getName(), message.get(blueocean_job_pipeline_name));
            if(message.get(jenkins_event).equals(Events.JobChannel.job_run_queue_left.name())) {
                assertionHelper.isEquals("1", message.get(blueocean_queue_item_expected_build_number));
            }
            assertionHelper.isEquals(p.getName(), message.get(job_name));
            assertionHelper.isEquals("job", message.get(jenkins_channel));
            assertionHelper.isEquals("jenkins", message.get(jenkins_org));
            assertionHelper.isNull(message.get(job_ismultibranch));
            assertionHelper.isNull(message.get(job_multibranch_indexing_result));
            assertionHelper.isNull(message.get(job_multibranch_indexing_status));
            assertionHelper.isNotNull(message.get(Job.job_run_queueId));
            assertionHelper.isNotNull(message.get(Job.job_run_status));

            if ("SUCCESS".equals(message.get(Job.job_run_status))
                    && "job_run_ended".equals(message.get(jenkins_event)))
                success.signal();
        } );
        con.subscribe("job");
        p.scheduleBuild2(0).get();

        // make sure we got the event we were looking for
        success.block(5000);
        con.close();

        if(assertionHelper.totalErrors() > 0){
            fail("There were errors: "+ assertionHelper.totalErrors());
        }
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
        final AssertionHelper assertionHelper = new AssertionHelper();
        SSEConnection con = new SSEConnection( j.getURL(), "me", message -> {
            System.out.println(message);
            if("job".equals(message.get(jenkins_channel))) {
                assertionHelper.isEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/", message.get(blueocean_job_rest_url));
                assertionHelper.isEquals("pipeline1", message.get(blueocean_job_pipeline_name));
                if(message.get(jenkins_event).equals(Events.JobChannel.job_run_queue_left.name())) {
                    assertionHelper.isEquals("1", message.get(blueocean_queue_item_expected_build_number));
                    assertionHelper.isNotNull(message.get(Job.job_run_queueId));
                    assertionHelper.isNotNull(message.get(Job.job_run_status));
                }
                assertionHelper.isEquals("pipeline1", message.get(job_name));
                assertionHelper.isEquals("job", message.get(jenkins_channel));
                assertionHelper.isEquals("jenkins", message.get(jenkins_org));
                assertionHelper.isNull(message.get(job_ismultibranch));
                assertionHelper.isNull(message.get(job_multibranch_indexing_result));
                assertionHelper.isNull(message.get(job_multibranch_indexing_status));

                if("job_run_unpaused".equals(message.get(jenkins_event))){
                    wasUnPaused[0] = true;
                }
            }else if("pipeline".equals(message.get(jenkins_channel))){
                assertionHelper.isEquals("1", message.get(pipeline_run_id));
                if(message.get(jenkins_event).equals(pipeline_stage.name())) {
                    assertionHelper.isEquals("build", message.get(pipeline_step_stage_name));
                }
                if("input".equals(message.get(pipeline_step_name))){
                    wasPaused[0] = true;
                    assertionHelper.isEquals("true", message.get(pipeline_step_is_paused));
                }
            }
            if(wasPaused[0] && wasUnPaused[0]){ // signal finish only when both conditions are met
                success.signal();
            }
        } );

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
        if(assertionHelper.totalErrors() > 0){
            fail("There were errors: "+ assertionHelper.totalErrors());
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
        final boolean[] masterBranchNodeBlockEvent = {false};
        final boolean[] feature1BranchNodeBlockEvent = {false};

        final AssertionHelper assertionHelper = new AssertionHelper();

        SSEConnection con = new SSEConnection( j.getURL(), "me", message -> {
            System.out.println(message);
            if("job".equals(message.get(jenkins_channel))) {
                if ("org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject".equals(message.get(jenkins_object_type))) {
                    assertionHelper.isEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/", message.get(blueocean_job_rest_url));

                    //assertEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/", message.get(blueocean_job_rest_url));
                    assertionHelper.isEquals("pipeline1", message.get(blueocean_job_pipeline_name));
                    assertionHelper.isEquals("job", message.get(jenkins_channel));
                    assertionHelper.isEquals("jenkins", message.get(jenkins_org));
                    assertionHelper.isEquals("true", message.get(job_ismultibranch));
                    assertionHelper.isEquals("pipeline1", message.get(job_name));
                    assertionHelper.isNull(message.get(job_orgfolder_indexing_status));
                    assertionHelper.isNull(message.get(job_orgfolder_indexing_result));
                    if ("QUEUED".equals(message.get(job_run_status))) {
                        mbpStatus[0] = true; // queued
                    }
                    if ("ALLOCATED".equals(message.get(job_run_status))) {
                        mbpStatus[1] = true; // allocated or left queue
                    }
                    if ("INDEXING".equals(message.get(job_multibranch_indexing_status))) {
                        mbpStatus[2] = true; // indexing started
                        assertionHelper.isNotNull(message.get(job_run_queueId));
                    }

                    if ("SUCCESS".equals(message.get(job_multibranch_indexing_result)) ||
                            ("COMPLETED".equals(message.get(job_multibranch_indexing_status))
                                    && "job_run_queue_task_complete".equals(message.get(jenkins_event)))
                            ) {
                        mbpStatus[3] = true; // indexing completed
                    }
                } else if ("org.jenkinsci.plugins.workflow.job.WorkflowJob".equals(message.get(jenkins_object_type))) {
                    assertionHelper.isEquals("pipeline1", message.get(blueocean_job_pipeline_name));
                    if ("pipeline1/master".equals(message.get(job_name))) {
                        System.out.println("job_run_status::::: " + message.get(job_run_status));
                        assertionHelper.isEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/branches/master/", message.get(blueocean_job_rest_url));
                        assertionHelper.isEquals("master", message.get(blueocean_job_branch_name));
                        if ("job_crud_created".equals(message.get(jenkins_event))) {
                            masterBranchStatus[0] = true;
                        }
                        if ("QUEUED".equals(message.get(job_run_status))) {
                            masterBranchStatus[1] = true;
                        } else if ("ALLOCATED".equals(message.get(job_run_status))) {
                            masterBranchStatus[2] = true;
                        } else if ("RUNNING".equals(message.get(job_run_status))) {
                            System.out.println("in master running.....");
                            assertionHelper.isEquals("1", message.get(blueocean_queue_item_expected_build_number));
                            masterBranchStatus[3] = true;
                        }

                        if ("SUCCESS".equals(message.get(job_run_status))
                                || "job_run_queue_task_complete".equals(message.get(jenkins_event))) {
                            masterBranchStatus[4] = true;
                        }
                    } else if ("pipeline1/feature%2Fux-1".equals(message.get(job_name))) {
                        assertionHelper.isEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/branches/feature%252Fux-1/", message.get(blueocean_job_rest_url));
                        assertionHelper.isEquals("feature%2Fux-1", message.get(blueocean_job_branch_name));
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
                            assertionHelper.isEquals("1", message.get(blueocean_queue_item_expected_build_number));
                            masterBranchStatus[3] = true;
                        }
                    } else if ("pipeline1/feature%2Fux-1".equals(message.get(job_name))) {
                        if ("RUNNING".equals(message.get(job_run_status))) {
                            assertionHelper.isEquals("1", message.get(blueocean_queue_item_expected_build_number));
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
                    assertionHelper.isEquals("1", message.get(pipeline_run_id));
                    masterBranchPipelineEvent[0]=true;
                    if ("pipeline_step".equals(message.get(jenkins_event)) &&
                        "node".equals(message.get(pipeline_step_name))) {
                        masterBranchNodeBlockEvent[0] = true;
                    }
                } else if("pipeline1/feature%2Fux-1".equals(message.get(pipeline_job_name))){
                    feature1BranchPipelineEvent[0]=true;
                    assertionHelper.isEquals("1", message.get(pipeline_run_id));
                    if ("pipeline_step".equals(message.get(jenkins_event)) &&
                        "node".equals(message.get(pipeline_step_name))) {
                        feature1BranchNodeBlockEvent[0] = true;
                    }
                }
                if("pipeline_stage".equals(message.get(jenkins_event))){
                    assertionHelper.isNotNull(message.get(pipeline_step_stage_name));
                    assertionHelper.isEquals("build", message.get(pipeline_step_stage_name));
                    assertionHelper.isNotNull(message.get(pipeline_step_flownode_id));
                }else if("pipeline_step".equals(message.get(jenkins_event))){
                    assertionHelper.isNotNull(message.get(pipeline_step_flownode_id));
                    assertionHelper.isEquals("false", message.get(pipeline_step_is_paused));
                }
            }
        } );
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
            assertTrue(masterBranchNodeBlockEvent[0]);
            assertTrue(feature1BranchNodeBlockEvent[0]);
            assertArrayEquals(mbpStatus, new boolean[]{true, true, true, true});
            assertArrayEquals(masterBranchStatus, new boolean[]{true, true, true, true, true});
            assertArrayEquals(feature1BranchStatus, new boolean[]{true, true, true, true, true});
        }

        if(assertionHelper.totalErrors() > 0){
            fail("There were errors: "+ assertionHelper.totalErrors());
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
            public void onMessage(@NonNull Message message) {
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
        private OrganizationImpl instance = new OrganizationImpl("TestOrg", Jenkins.get().getItem("/TestOrgFolderName", Jenkins.get(), MockFolder.class));

        @Override
        public OrganizationImpl get(String name) {
            if (instance != null) {
                if (instance.getName().equals(name)) {
                    System.out.println("" + name + " Instance returned " + instance);
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
            if (group == instance.getGroup() || group == Jenkins.get()) {
                return instance;
            }
            return null;
        }
    }

    private static class AssertionHelper {
        private final AtomicInteger errorCount = new AtomicInteger();

        public void isEquals(String source, String target){
            if(!source.equals(target)){
                errorCount.incrementAndGet();
                fail(String.format("[ERROR] Source: %s is not equals to target: %s", source, target));
            }
        }

        public void isNull(String target){
            if(target != null){
                errorCount.incrementAndGet();
                fail("[ERROR] "+target +" is expected to be null");
            }
        }

        public void isNotNull(String target){
            if(target == null){
                errorCount.incrementAndGet();
                fail("[ERROR] Expected non-null value");
            }
        }

        public int totalErrors(){
            return errorCount.get();
        }
    }
}
