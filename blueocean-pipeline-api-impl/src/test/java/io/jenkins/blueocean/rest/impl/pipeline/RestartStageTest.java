package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.Label;
import io.jenkins.blueocean.service.embedded.rest.QueueItemImpl;
import io.jenkins.blueocean.service.embedded.rest.QueuedBlueRun;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class RestartStageTest extends PipelineBaseTest
{

    @Test
    public void restart_stage() throws Exception {
        WorkflowJob p = createWorkflowJobWithJenkinsfile( getClass(), "restartStage.jenkinsfile");

        // Ensure null before first run
        Map pipeline = request().get( String.format( "/organizations/jenkins/pipelines/%s/", p.getName())).build( Map.class);
        Assert.assertNull( pipeline.get( "latestRun"));
        j.createOnlineSlave( Label.get( "first"));

        // Run until completed
        WorkflowRun r = p.scheduleBuild2( 0).waitForStart();
        j.waitForCompletion( r );

        // Ensure we find stage with restartable flag
        List<Map> resp = get( "/organizations/jenkins/pipelines/" + p.getName() + "/runs/1/nodes/", List.class);
        assertEquals(2, resp.size());

        Optional<Map> optionalMap = resp.stream()
            .filter( map -> map.get( "displayName" ).equals( "Stage test" ) )
            .findFirst();
        assertTrue(optionalMap.isPresent());

        Map res = optionalMap.get();
        assertEquals( true, res.get( "restartable" ) );

        LOGGER.info( "buildNumber: {}", r.getNumber() );

        // restart the stage
        Map restartMap = new HashMap( 1 );
        restartMap.put( "restart", true );
        Map restartResult = post( "/organizations/jenkins/pipelines/" + p.getName()
                                      + "/runs/1/nodes/" + res.get( "id" ) + "/",
                                  restartMap);

        assertEquals( QueuedBlueRun.class.getName(), restartResult.get( "_class" ) );
        int id = Integer.parseInt((String)restartResult.get( "id" ));

        // depending on build still in queue or not when guessing the build number
        assertTrue(  id >= r.getNumber());

        //check if buid finished ??

        Map runResult = get( "/organizations/jenkins/pipelines/" + p.getName() + "/runs/2");
        LOGGER.info( "runResult: {}", runResult );



    }
}
