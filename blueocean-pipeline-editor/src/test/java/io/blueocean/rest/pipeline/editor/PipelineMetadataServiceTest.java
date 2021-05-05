package io.blueocean.rest.pipeline.editor;

import hudson.model.JDK;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.hamcrest.Matcher;
import org.jenkinsci.plugins.pipeline.modeldefinition.agent.impl.Label;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.JSONWebResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.*;


/**
 * Basic tests for {@link PipelineMetadataService}
 */
public class PipelineMetadataServiceTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testBasicStepsReturned() throws IOException {
        JSONWebResponse rsp = j.getJSON("blue/rest/pipeline-metadata/pipelineStepMetadata");
        assert(rsp != null) : "Should have results";
        JSONObject node = null;
        JSONObject withSonarQubeEnv = null;
        for (Object o : JSONArray.fromObject(rsp.getContentAsString())) {
            JSONObject meta = (JSONObject)o;
            if("node".equals(meta.get("functionName"))) {
                node = meta;
            } else if ("withSonarQubeEnv".equals(meta.get("functionName"))) {
                withSonarQubeEnv = meta;
            }
        }
        assert(node != null) : "PipelineStepMetadata node not found";
        assert(withSonarQubeEnv != null) : "PipelineStepMetadata withSonarQubeEnv not found";
    }

    @Test
    public void declarativeAgents() throws Exception {
        PipelineMetadataService svc = new PipelineMetadataService();

        List<ExportedDescribableModel> agents = new ArrayList<>();
        agents.addAll(Arrays.asList(svc.doAgentMetadata()));

        assertFalse(agents.isEmpty());

        ExportedDescribableModel m = null;

        for (ExportedDescribableModel a : agents) {
            if (a.getType().endsWith(Label.class.getName())) {
                m = a;
            }
        }

        assertNotNull(m);

        assertTrue(m.getHasSingleRequiredParameter());

        // check there are parameters present
        assertTrue(m.getParameters().size() > 0);
    }

    @Test
    public void toolMetadata() throws Exception {
        PipelineMetadataService svc = new PipelineMetadataService();

        List<ExportedToolDescriptor> tools = new ArrayList<>();
        tools.addAll(Arrays.asList(svc.doToolMetadata()));

        assertFalse(tools.isEmpty());

        ExportedToolDescriptor t = null;

        for (ExportedToolDescriptor a : tools) {
            if (a.getType().equals(JDK.DescriptorImpl.class.getName())) {
                t = a;
            }
        }

        assertNotNull(t);

        assertEquals("jdk", t.getSymbol());
    }

    @Test
    public void wrappers() throws Exception {
        PipelineMetadataService svc = new PipelineMetadataService();

        List<ExportedPipelineStep> wrappers = new ArrayList<>();
        wrappers.addAll(Arrays.asList(svc.doWrapperMetadata()));

        assertFalse(wrappers.isEmpty());

        ExportedPipelineStep w = null;

        for (ExportedPipelineStep s : wrappers) {
            if (s.getFunctionName().equals("timeout")) {
                w = s;
            }
        }

        assertNotNull(w);
    }

    @Test
    public void verifyFunctionNames() throws Exception {
        PipelineMetadataService svc = new PipelineMetadataService();

        List<ExportedDescribableModel> steps = new ArrayList<>();

        steps.addAll(Arrays.asList(svc.doPipelineStepMetadata()));

        assertFalse(steps.isEmpty());

        // Verify we have a Symbol-provided Builder or Publisher
        assertThat(steps, hasItem(stepWithName("archiveArtifacts")));

        // Verify that we don't have steps blacklisted by Declarative
        assertThat(steps, not(hasItem(stepWithName("properties"))));

        // Verify that we don't have advanced steps
        assertThat(steps, not(hasItem(stepWithName("archive"))));

        // Verify that we *do* have advanced steps that are explicitly whitelisted in.
        assertThat(steps, hasItem(stepWithName("catchError")));

        // Verify that we have a Symbol-provided SimpleBuildWrapper
        assertThat(steps, hasItem(stepWithName("withSonarQubeEnv")));
    }

    private Matcher<? super ExportedPipelineStep> stepWithName(String stepName) {
        return hasProperty("functionName", is(stepName));
    }
}
