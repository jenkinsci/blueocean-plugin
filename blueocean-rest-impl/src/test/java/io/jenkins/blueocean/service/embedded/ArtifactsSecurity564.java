package io.jenkins.blueocean.service.embedded;

import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Run;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.HudsonPrivateSecurityRealm;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Shell;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.util.List;
import java.util.Map;

public class ArtifactsSecurity564 extends BaseTest {
    static {
        System.setProperty("hudson.security.ArtifactsPermission", "true");
    }

    /**
     * Uses matrix-auth to provide artifacts permission.
     *
     * If hudson.security.ArtifactsPermission is set then the user must have Run.ARTIFACTS set.
     * 
     * @throws Exception
     */
    @Issue("SECURITY-564")
    @Test
    public void testArtifactsWithPermissions() throws Exception {
        String JOB_NAME = "artifactPermissions";
        HudsonPrivateSecurityRealm realm = new HudsonPrivateSecurityRealm(false);
        realm.createAccount("alice","alice");
        realm.createAccount("bob","bob");
        j.jenkins.setSecurityRealm(realm);

        GlobalMatrixAuthorizationStrategy as = new GlobalMatrixAuthorizationStrategy();
        j.jenkins.setAuthorizationStrategy(as);
        as.add(Hudson.READ,"alice");
        as.add(Item.READ,"alice");
        as.add(Run.ARTIFACTS,"alice");

        as.add(Hudson.READ,"bob");
        as.add(Item.READ,"bob");

        FreeStyleProject p = j.createFreeStyleProject(JOB_NAME);
        p.getBuildersList().add(new Shell("#!/bin/bash\nmkdir -p test/me/out; cd test/me/out; touch {0..105}.txt"));
        p.getPublishersList().add(new ArtifactArchiver("**/*"));
        Run r = p.scheduleBuild2(0).waitForStart();

        r = j.waitForCompletion(r);

        List artifacts = request().authAlice().get("/organizations/jenkins/pipelines/"+JOB_NAME+"/runs/"+r.getId()+"/artifacts").build(List.class);

        Assert.assertEquals(100, artifacts.size());
        Assert.assertEquals(0, ((Map) artifacts.get(0)).get("size"));
        Assert.assertEquals("test/me/out/0.txt", ((Map) artifacts.get(0)).get("path"));
        Assert.assertEquals("/job/artifactPermissions/1/artifact/test/me/out/0.txt", ((Map) artifacts.get(0)).get("url"));

        List artifactsBob = request().auth("bob", "bob").get("/organizations/jenkins/pipelines/"+JOB_NAME+"/runs/"+r.getId()+"/artifacts").build(List.class);

        Assert.assertEquals(0, artifactsBob.size());
    }

}
