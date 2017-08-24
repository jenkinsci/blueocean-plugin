package io.jenkins.blueocean.service.embedded;


import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Shell;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class ArtifactContainerImplTest extends BaseTest {
    public static final String JOB_NAME = "artifactTest";

    @Test
    public void testArtifactsListing() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject(JOB_NAME);
        p.getBuildersList().add(new Shell("#!/bin/bash\nmkdir -p test/me/out; cd test/me/out; touch {0..105}.txt"));
        p.getPublishersList().add(new ArtifactArchiver("**/*"));
        Run r = p.scheduleBuild2(0).waitForStart();

        r = j.waitForCompletion(r);

        List artifacts = request().get("/organizations/jenkins/pipelines/"+JOB_NAME+"/runs/"+r.getId()+"/artifacts").build(List.class);

        Assert.assertEquals(100, artifacts.size());
        Assert.assertEquals(0, ((Map) artifacts.get(0)).get("size"));
        Assert.assertEquals("test/me/out/0.txt", ((Map) artifacts.get(0)).get("path"));
        Assert.assertEquals("/job/artifactTest/1/artifact/test/me/out/0.txt", ((Map) artifacts.get(0)).get("url"));
     }

    // TODO: needs viveks input
    @Test @Ignore
    public void testArtifact() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject(JOB_NAME);
        p.getBuildersList().add(new Shell("mkdir -p test/me/out; touch test/me/out/{{a..z},{A..Z},{0..99}}.txt"));
        p.getPublishersList().add(new ArtifactArchiver("**/*"));
        Run r = p.scheduleBuild2(0).waitForStart();

        r = j.waitForCompletion(r);

        Map artifact = request().get("/organizations/jenkins/pipelines/"+JOB_NAME+"/runs/"+r.getId()+"/artifacts/test%252Fme%252Fout%252F0.txt").build(Map.class);

        Assert.assertEquals(100, artifact.size());
    }


}
