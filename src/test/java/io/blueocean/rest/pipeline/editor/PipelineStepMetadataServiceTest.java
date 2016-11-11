package io.blueocean.rest.pipeline.editor;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.JSONWebResponse;
import org.jvnet.hudson.test.recipes.WithPlugin;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * Basic tests for {@link PipelineStepMetadataService}
 */
public class PipelineStepMetadataServiceTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    // FIXME: this isn't actually registering extensions properly, unclear why, but returning 404
    //@Test
    public void testBasicStepsReturned() throws IOException {
        JSONWebResponse rsp = j.getJSON("blue/rest/pipeline-step-metadata/");
        
        assert(rsp != null) : "Should have results";
        JSONObject node = null;
        for (Object o : JSONArray.fromObject(rsp.getContentAsString())) {
            JSONObject meta = (JSONObject)o;
            if("node".equals(meta.get("functionName"))) {
                node = meta;
                break;
            }
        }
        assert(node != null) : "PipelineStepMetadata node not found";
    }
}
