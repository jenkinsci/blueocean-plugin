package io.blueocean.rest.pipeline.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.JSONWebResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;


/**
 * Basic tests for {@link PipelineMetadataService}
 */
public class PipelineMetadataServiceTest {
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

    @Test
    public void verifyFunctionNames() throws Exception {
        PipelineMetadataService svc = new PipelineMetadataService();

        List<ExportedDescribableModel> steps = new ArrayList<>();

        steps.addAll(Arrays.asList(svc.getPipelineStepMetadata()));

        assertFalse(steps.isEmpty());
        
        // Verify we have a Symbol-provided Builder or Publisher
        assertThat(steps, hasItem(stepWithName("archiveArtifacts")));

        // Verify that we don't have steps blacklisted by Declarative
        assertThat(steps, not(hasItem(stepWithName("properties"))));

        // Verify that we don't have advanced steps
        assertThat(steps, not(hasItem(stepWithName("archive"))));

        // Verify that we *do* have advanced steps that are explicitly whitelisted in.
        assertThat(steps, hasItem(stepWithName("catchError")));
    }

    private Matcher<? super ExportedPipelineStep> stepWithName(String stepName) {
        return hasProperty("functionName", is(stepName));
    }
}
