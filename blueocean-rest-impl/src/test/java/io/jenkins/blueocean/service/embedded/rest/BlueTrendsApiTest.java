package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.model.Run;
import io.jenkins.blueocean.service.embedded.BaseTest;
import io.jenkins.blueocean.service.embedded.rest.junit.BlueJUnitTrend;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;


/**
 * @author cliffmeyers
 */
public class BlueTrendsApiTest extends BaseTest {

    @Test
    public void getTrendsListFreestyle() throws IOException {
        Project p = j.createProject(FreeStyleProject.class, "freestyle1");

        List response = new RequestBuilder(baseUrl)
            .get("/organizations/jenkins/pipelines/"+p.getName()+"/trends/")
            .build(List.class);

        Assert.assertNotNull(response);
    }

    @Test
    public void getJUnitTrends() throws Exception {
        URL resource = Resources.getResource(getClass(), "BlueJUnitTestResultTest.jenkinsfile");
        String jenkinsFile = Resources.toString(resource, Charsets.UTF_8);
        WorkflowJob p = j.createProject(WorkflowJob.class, "project");
        p.setDefinition(new CpsFlowDefinition(jenkinsFile, false));
        p.save();

        Run r = p.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(r);

        Map response = new RequestBuilder(baseUrl)
            .get("/organizations/jenkins/pipelines/"+p.getName()+"/trends/junit/")
            .build(Map.class);

        Assert.assertNotNull(response);
        Map columns = (Map) response.get("columns");
        Assert.assertNotNull(columns);
        Assert.assertEquals(7, columns.keySet().size());

        List rows = (List) response.get("rows");
        Assert.assertNotNull(rows);
        Assert.assertEquals(1, rows.size());
        Map testRow = (Map) rows.get(0);
        Assert.assertEquals(3, testRow.get(BlueJUnitTrend.TOTAL));
        Assert.assertEquals(2, testRow.get(BlueJUnitTrend.PASSED));
        Assert.assertEquals(1, testRow.get(BlueJUnitTrend.FAILED));
        Assert.assertEquals(1, testRow.get(BlueJUnitTrend.EXISTING_FAILED));
        Assert.assertEquals(0, testRow.get(BlueJUnitTrend.FIXED));
        Assert.assertEquals(0, testRow.get(BlueJUnitTrend.REGRESSIONS));
        Assert.assertEquals(0, testRow.get(BlueJUnitTrend.SKIPPED));
    }
}
