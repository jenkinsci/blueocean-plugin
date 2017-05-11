package io.jenkins.blueocean.service.embedded;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PipelineSearchTest extends BaseTest {
    @Test
    public void testStartsWithSearch() throws IOException {
        FreeStyleProject aa = j.createFreeStyleProject("aa");
        FreeStyleProject bb = j.createFreeStyleProject("bb");

        List req = request()
            .get("/search/?q=type:pipeline;startsWith:a")
            .build(List.class);

        Assert.assertEquals(1, req.size());
        Assert.assertEquals("aa", ((Map) req.get(0)).get("name"));
    }

    @Test
    public void testOrganizationSearch() throws IOException {
        FreeStyleProject aa = j.createFreeStyleProject("aa");
        FreeStyleProject bb = j.createFreeStyleProject("bb");

        List req = request()
            .get("/search/?q=type:pipeline;startsWith:a;organization:jenkins")
            .build(List.class);

        Assert.assertEquals(1, req.size());
        Assert.assertEquals("aa", ((Map) req.get(0)).get("name"));
    }

    @Test
    public void testSearchPipeline() throws IOException {
        FreeStyleProject aa = j.createFreeStyleProject("aa");
        FreeStyleProject bb = j.createFreeStyleProject("bb");

        List req = request()
            .get("/search/?q=type:pipeline")
            .build(List.class);

        Assert.assertEquals(2, req.size());
        Assert.assertEquals("aa", ((Map) req.get(0)).get("name"));
        Assert.assertEquals("bb", ((Map) req.get(1)).get("name"));
    }

    @Test
    public void testSearchOrganizationPipeline() throws IOException {
        FreeStyleProject aa = j.createFreeStyleProject("aa");
        FreeStyleProject bb = j.createFreeStyleProject("bb");

        List req = request()
            .get("/search/?q=type:pipeline;organization:jenkins")
            .build(List.class);

        Assert.assertEquals(2, req.size());
        Assert.assertEquals("aa", ((Map) req.get(0)).get("name"));
        Assert.assertEquals("bb", ((Map) req.get(1)).get("name"));
    }
}
