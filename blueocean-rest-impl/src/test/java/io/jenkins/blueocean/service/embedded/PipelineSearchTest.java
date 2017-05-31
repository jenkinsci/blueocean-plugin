package io.jenkins.blueocean.service.embedded;

import hudson.model.FreeStyleProject;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.MockFolder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PipelineSearchTest extends BaseTest {
    @Test
    public void testOrganizationSearch() throws IOException {
        j.createFreeStyleProject("aa");
        j.createFreeStyleProject("bb");
        MockFolder folder = j.createFolder("Cool");
        folder.createProject(FreeStyleProject.class, "aa");
        folder.createProject(FreeStyleProject.class, "yy");
        folder.createProject(FreeStyleProject.class, "zz");

        // user types "a"
        List req = request()
            .get("/search/?q=type:pipeline;pipeline:*a*;organization:jenkins")
            .build(List.class);

        Assert.assertEquals(2, req.size());
        Assert.assertEquals("aa", ((Map) req.get(0)).get("fullName"));
        Assert.assertEquals("Cool/aa", ((Map) req.get(1)).get("fullName"));
    }

    @Test
    public void testOrganizationSearchWithKnownPrefix() throws IOException {
        j.createFreeStyleProject("aa");
        j.createFreeStyleProject("bb");
        MockFolder folder = j.createFolder("Cool");
        folder.createProject(FreeStyleProject.class, "yy");
        folder.createProject(FreeStyleProject.class, "zz");

        // user types "cool/"
        List req = request()
            .get("/search/?q=type:pipeline;pipeline:Cool/*;organization:jenkins")
            .build(List.class);

        Assert.assertEquals(2, req.size());
        Assert.assertEquals("Cool/yy", ((Map) req.get(0)).get("fullName"));
        Assert.assertEquals("Cool/zz", ((Map) req.get(1)).get("fullName"));
    }

    @Test
    public void testOrganizationSearchWithPartialPath() throws IOException {
        j.createFreeStyleProject("aa");
        j.createFreeStyleProject("bb");
        MockFolder folder = j.createFolder("Cool");
        folder.createProject(FreeStyleProject.class, "yy");
        folder.createProject(FreeStyleProject.class, "zz");

        // user types "c/z"
        List req = request()
            .get("/search/?q=type:pipeline;pipeline:*c*/*z*;organization:jenkins")
            .build(List.class);

        Assert.assertEquals(1, req.size());
        Assert.assertEquals("Cool/zz", ((Map) req.get(0)).get("fullName"));
    }

    @Test
    public void testOrganizationSearchByName() throws IOException {
        j.createFreeStyleProject("aa");
        j.createFreeStyleProject("bb");

        // user types "aa"
        List req = request()
            .get("/search/?q=type:pipeline;pipeline:aa;organization:jenkins")
            .build(List.class);

        Assert.assertEquals(1, req.size());
        Assert.assertEquals("aa", ((Map) req.get(0)).get("name"));
    }

    @Test
    public void testSearchPipeline() throws IOException {
        j.createFreeStyleProject("aa");
        j.createFreeStyleProject("bb");

        List req = request()
            .get("/search/?q=type:pipeline")
            .build(List.class);

        Assert.assertEquals(2, req.size());
        Assert.assertEquals("aa", ((Map) req.get(0)).get("name"));
        Assert.assertEquals("bb", ((Map) req.get(1)).get("name"));
    }

    @Test
    public void testSearchOrganizationPipeline() throws IOException {
        j.createFreeStyleProject("aa");
        j.createFreeStyleProject("bb");

        List req = request()
            .get("/search/?q=type:pipeline;organization:jenkins")
            .build(List.class);

        Assert.assertEquals(2, req.size());
        Assert.assertEquals("aa", ((Map) req.get(0)).get("name"));
        Assert.assertEquals("bb", ((Map) req.get(1)).get("name"));
    }
}
