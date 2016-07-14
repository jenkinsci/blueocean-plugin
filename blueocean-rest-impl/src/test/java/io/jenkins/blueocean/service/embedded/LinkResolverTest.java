package io.jenkins.blueocean.service.embedded;

import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.model.Run;
import io.jenkins.blueocean.rest.hal.LinkResolver;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.MockFolder;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author Vivek Pandey
 */
public class LinkResolverTest extends BaseTest {

    @Override
    public void setup() throws Exception {
        super.setup();
    }

    @Test
    public void nestedFolderJobLinkResolveTest() throws IOException {
        Project f = j.createFreeStyleProject("fstyle1");
        MockFolder folder1 = j.createFolder("folder1");
        Project p1 = folder1.createProject(FreeStyleProject.class, "test1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "folder2");
        MockFolder folder3 = folder2.createProject(MockFolder.class, "folder3");
        Project p2 = folder2.createProject(FreeStyleProject.class, "test2");
        Project p3 = folder3.createProject(FreeStyleProject.class, "test3");

        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/fstyle1/",LinkResolver.resolveLink(f).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/",LinkResolver.resolveLink(folder1).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/test1/",LinkResolver.resolveLink(p1).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/",LinkResolver.resolveLink(folder2).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/test2/",LinkResolver.resolveLink(p2).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/",LinkResolver.resolveLink(folder3).getHref());
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines/test3/",LinkResolver.resolveLink(p3).getHref());
    }


    @Test
    public void runLinkResolveTest() throws IOException, ExecutionException, InterruptedException {
        Project f = j.createFreeStyleProject("fstyle1");
        MockFolder folder1 = j.createFolder("folder1");
        Project p1 = folder1.createProject(FreeStyleProject.class, "test1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "folder2");
        MockFolder folder3 = folder2.createProject(MockFolder.class, "folder3");
        Project p2 = folder2.createProject(FreeStyleProject.class, "test2");
        Project p3 = folder3.createProject(FreeStyleProject.class, "test3");

        Run r = (Run) f.scheduleBuild2(0).get();
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/fstyle1/runs/"+r.getId()+"/",LinkResolver.resolveLink(r).getHref());

        r = (Run) p1.scheduleBuild2(0).get();
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/test1/runs/"+r.getId()+"/",LinkResolver.resolveLink(r).getHref());

        r = (Run) p2.scheduleBuild2(0).get();
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/test2/runs/"+r.getId()+"/",LinkResolver.resolveLink(r).getHref());

        r = (Run) p3.scheduleBuild2(0).get();
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines/test3/runs/"+r.getId()+"/",LinkResolver.resolveLink(r).getHref());
    }

}
