package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.FilePath;
import hudson.model.Label;
import io.jenkins.blueocean.service.embedded.rest.QueuedBlueRun;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RestartStageTest extends PipelineBaseTest
{

    @Test
    public void restart_stage() throws Exception {
        WorkflowJob p = createWorkflowJobWithJenkinsfile( getClass(), "restartStage.jenkinsfile");

        // Ensure null before first run
        Map pipeline = request().get( String.format( "/organizations/jenkins/pipelines/%s/", p.getName())).build( Map.class);
        Assert.assertNull( pipeline.get( "latestRun"));
        j.createOnlineSlave( Label.get( "first"));

        FilePath ws = j.jenkins.getWorkspaceFor(p);
        FilePath noErrorMessageFile = ws.child( "TEST-io.blueocean.NoErrorMessage.xml");
        noErrorMessageFile.copyFrom(RestartStageTest.class.getResource("TEST-io.blueocean.NoErrorMessage.xml"));

        FilePath stdoutStderrFile = ws.child( "TEST-io.blueocean.StdoutStderr.xml");
        stdoutStderrFile.copyFrom(RestartStageTest.class.getResource("TEST-io.blueocean.StdoutStderr.xml"));

        // Run until completed
        WorkflowRun r = p.scheduleBuild2( 0).waitForStart();
        j.waitForCompletion( r );

        Map runResult = get( "/organizations/jenkins/pipelines/" + p.getName() + "/runs/1");
        while (runResult.get( "state" ).equals( "RUNNING" )) {
            runResult = get( "/organizations/jenkins/pipelines/" + p.getName() + "/runs/1");
        }

        Assert.assertEquals( "SUCCESS", runResult.get( "result" ) );

        // Ensure we find stage with restartable flag
        List<Map> resp = get( "/organizations/jenkins/pipelines/" + p.getName() + "/runs/1/nodes/", List.class);
        assertEquals(10, resp.size());

        Optional<Map> optionalMap = resp.stream()
            .filter( map -> map.get( "displayName" ).equals( "Static Analysis" ) )
            .findFirst();
        assertTrue(optionalMap.isPresent());

        Map res = optionalMap.get();
        assertEquals( true, res.get( "restartable" ) );

        LOGGER.info( "buildNumber: {}", r.getNumber() );

        noErrorMessageFile.delete();
        stdoutStderrFile.delete();

        // restart the stage
        Map restartMap = new HashMap( 1 );
        restartMap.put( "restart", true );
        Map restartResult = post( "/organizations/jenkins/pipelines/" + p.getName()
                                      + "/runs/1/nodes/" + res.get( "id" ) + "/restart",
                                  restartMap);

        assertEquals( QueuedBlueRun.class.getName(), restartResult.get( "_class" ) );
        int id = Integer.parseInt((String)restartResult.get( "id" ));

        // depending on build still in queue or not when guessing the build number
        assertTrue(  id >= r.getNumber());

        // wait until the build get started
        r = p.getBuildByNumber( 2 );
        while(r==null){
            Thread.sleep( 100 );
            r = p.getBuildByNumber( 2 );
        }
        j.waitForCompletion( r );

        runResult = get( "/organizations/jenkins/pipelines/" + p.getName() + "/runs/2");
        while (runResult.get( "state" ).equals( "RUNNING" )) {
            runResult = get( "/organizations/jenkins/pipelines/" + p.getName() + "/runs/2");
        }
        LOGGER.info( "runResult: {}", runResult );
        Assert.assertNotEquals( "FAILURE", runResult.get( "result" ) );
    }
}
