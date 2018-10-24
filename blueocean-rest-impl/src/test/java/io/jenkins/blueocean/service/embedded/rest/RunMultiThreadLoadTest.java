package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RunMultiThreadLoadTest
{
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @BeforeClass
    public static void activate_multi_threaded() {
        System.setProperty( RunSearch.COLLECT_THREADS_KEY, "2" );
    }

    @AfterClass
    public static void desactivate_multi_threaded() {
        System.setProperty( RunSearch.COLLECT_THREADS_KEY, "0" );
    }

    @Test
    public void load_runs_multi_threaded() throws Exception {

        URL resource = Resources.getResource( getClass(), "RunMultiThreadLoadTest.jenkinsfile");
        String jenkinsFile = Resources.toString( resource, Charsets.UTF_8);
        WorkflowJob p = j.createProject( WorkflowJob.class, "project1");
        p.setDefinition(new CpsFlowDefinition( jenkinsFile, false));
        p.save();

        for (int i = 0; i < 10; i++) {
            j.waitForCompletion(p.scheduleBuild2(0).waitForStart());
        }
        Iterable<BlueRun> blueRuns = RunSearch.findRuns( p, null, 0, 20 );
        List<BlueRun> runs = StreamSupport.stream( blueRuns.spliterator(), false )
                                .collect(Collectors.toList());
        Assert.assertFalse( runs.isEmpty() );
        Assert.assertEquals( 10, runs.size() );
    }

    @Test
    @Issue( "JENKINS-52101" )
    public void load_runs_multi_threaded_no_runs() throws Exception {

        URL resource = Resources.getResource( getClass(), "RunMultiThreadLoadTest.jenkinsfile");
        String jenkinsFile = Resources.toString( resource, Charsets.UTF_8);
        WorkflowJob p = j.createProject( WorkflowJob.class, "project2");
        p.setDefinition(new CpsFlowDefinition( jenkinsFile, false));
        p.save();

        Iterable<BlueRun> blueRuns = RunSearch.findRuns( p, null, 0, 0 );
        List<BlueRun> runs = StreamSupport.stream( blueRuns.spliterator(), false )
            .collect(Collectors.toList());
        Assert.assertTrue( runs.isEmpty() );
        Assert.assertEquals( 0, runs.size() );
    }
}
