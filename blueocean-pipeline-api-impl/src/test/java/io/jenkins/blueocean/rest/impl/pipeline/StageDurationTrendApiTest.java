package io.jenkins.blueocean.rest.impl.pipeline;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static io.jenkins.blueocean.rest.impl.pipeline.StageDurationTrend.StageDurationTrendRow.NODES;

/**
 * @author cliffmeyers
 */
public class StageDurationTrendApiTest extends PipelineBaseTest {

    @Test
    public void getDurationTrend() throws Exception {
        URL resource = Resources.getResource(getClass(), "declarativeThreeStages.jenkinsfile");
        String jenkinsFile = Resources.toString(resource, Charsets.UTF_8);

        WorkflowJob p = j.createProject(WorkflowJob.class, "duration-trend");
        p.setDefinition(new CpsFlowDefinition(jenkinsFile, true));
        p.save();

        Run r = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(r);

        Map response = new RequestBuilder(baseUrl)
            .get("/organizations/jenkins/pipelines/"+p.getName()+"/trends/stageDuration/")
            .build(Map.class);

        Assert.assertNotNull(response);

        List rows = new RequestBuilder(baseUrl)
            .get("/organizations/jenkins/pipelines/"+p.getName()+"/trends/stageDuration/rows/")
            .build(List.class);

        Assert.assertNotNull(rows);
        Assert.assertEquals(1, rows.size());

        Map stageRow = (Map) rows.get(0);
        Map nodes = (Map) stageRow.get(NODES);
        Assert.assertNotNull(nodes.get("first"));
        Assert.assertNotNull(nodes.get("second"));
        Assert.assertNotNull(nodes.get("third"));
    }
}
