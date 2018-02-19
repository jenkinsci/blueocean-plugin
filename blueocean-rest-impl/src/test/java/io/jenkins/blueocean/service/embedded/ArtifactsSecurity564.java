package io.jenkins.blueocean.service.embedded;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Run;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.HudsonPrivateSecurityRealm;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.TouchBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class ArtifactsSecurity564 extends BaseTest {
    @BeforeClass
    public static void beforeClass() {
        System.setProperty("hudson.security.ArtifactsPermission", "true");
    }

    @AfterClass
    public static void afterClass() {
        System.setProperty("hudson.security.ArtifactsPermission", "false");
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
        String artifactPath = "a/b/c";
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
        p.getBuildersList().add(new ArtifactBuilder(artifactPath, 100));
        p.getPublishersList().add(new ArtifactArchiver("**/*"));
        Run r = p.scheduleBuild2(0).waitForStart();

        r = j.waitForCompletion(r);

        List artifacts = request().authAlice().get("/organizations/jenkins/pipelines/"+JOB_NAME+"/runs/"+r.getId()+"/artifacts").build(List.class);

        Assert.assertEquals(100, artifacts.size());
        Assert.assertEquals(0, ((Map) artifacts.get(0)).get("size"));
        Assert.assertEquals(artifactPath + "/0.txt", ((Map) artifacts.get(0)).get("path"));
        Assert.assertEquals("/job/artifactPermissions/1/artifact/"+ artifactPath +"/0.txt", ((Map) artifacts.get(0)).get("url"));

        List artifactsBob = request().auth("bob", "bob").get("/organizations/jenkins/pipelines/"+JOB_NAME+"/runs/"+r.getId()+"/artifacts").build(List.class);

        Assert.assertEquals(0, artifactsBob.size());
    }

    public static class ArtifactBuilder extends Builder implements Serializable {
        private String path;
        private int numberOfFiles;

        public ArtifactBuilder(String path, int numberOfFiles) {
            this.path = path;
            this.numberOfFiles = numberOfFiles;
        }

        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
            FilePath f = new FilePath(build.getWorkspace(), path);
            f.mkdirs();
            for (int i = 0; i < numberOfFiles; i++) {
                new FilePath(f, i + ".txt").touch(System.currentTimeMillis());
            }

            return true;
        }
    }
}
