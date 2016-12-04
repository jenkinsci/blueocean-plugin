package io.jenkins.blueocean.service.embedded;


import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Shell;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class ArtifactContainerImplTest extends BaseTest {
    public static final String JOB_NAME = "artifactTest";

    @Test
    public void testArtifactsListing() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject(JOB_NAME);
        p.getBuildersList().add(new Shell("touch {{a..z},{A..Z},{0..99}}.txt"));
        p.getPublishersList().add(new ArtifactArchiver("*"));
        Run r = p.scheduleBuild2(0).waitForStart();


        r = j.waitForCompletion(r);

        Map m = request().get("/organizations/jenkins/pipelines/"+JOB_NAME+"/runs/"+r.getId()+"/artifacts").build(Map.class);

        Assert.assertEquals(m.get("zipFile"), "/jenkins/job/artifactTest/1/artifact/*zip*/archive.zip");

        List artifacts = (List) m.get("artifacts");

        Assert.assertEquals(100, artifacts.size());
    }
}
