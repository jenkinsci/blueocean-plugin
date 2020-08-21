package io.jenkins.blueocean.rest.impl.pipeline.io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderPipelineImpl;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.branch.OrganizationFolder;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public class OrganizationFolderTest2 extends PipelineBaseTest {
    static class FakeOrg extends OrganizationFolderPipelineImpl {

        public FakeOrg(BlueOrganization organization, OrganizationFolder folder, Link parent) {
            super(organization, folder, parent);
        }

        @Extension(ordinal = -8)
        public static class OrganizationFolderFactoryImpl extends OrganizationFolderFactory {
            @Override
            protected OrganizationFolderPipelineImpl getFolder(jenkins.branch.OrganizationFolder folder, Reachable parent, BlueOrganization organization) {
                if (folder.getName() == "orgFolder1") {
                    return new FakeOrg(organization, folder, parent.getLink());
                }
                return null;
            }
        }
    }

    @Test
    public void testWorkflowPipieline() throws Exception {
        Map<String, Object> expectedDisabledResponse = new HashMap<>();
        expectedDisabledResponse.put("message", "Cannot disable this item");
        expectedDisabledResponse.put("code", Integer.valueOf(405));
        expectedDisabledResponse.put("errors", Collections.emptyList());

        OrganizationFolder orgFolder = j.jenkins.createProject(OrganizationFolder.class, "orgFolder1");
        login();
        assertNull(get("/organizations/jenkins/pipelines/" + orgFolder.getFullName() + "/").get("disabled"));

        assertThat(expectedDisabledResponse, is(put("/organizations/jenkins/pipelines/" + orgFolder.getFullName() + "/disable", "{}", 405)));
        assertThat(expectedDisabledResponse, is(put("/organizations/jenkins/pipelines/" + orgFolder.getFullName() + "/enable", "{}", 405)));
    }
}
