package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Job;
import hudson.security.ACL;
import hudson.security.ACLContext;
import io.jenkins.blueocean.service.embedded.BaseTest;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Assert;
import org.junit.Test;

public class AbstractPipelineImplTest extends BaseTest {
    @Test
    public void testFreestyle() throws Exception {
        Job job = j.createFreeStyleProject("freestyle");
        try (ACLContext ctx = ACL.as(user())) {
            Assert.assertEquals(
                get("/organizations/jenkins/pipelines/" + job.getFullName() + "/").get("disabled"),
                false
            );
            put("/organizations/jenkins/pipelines/" + job.getFullName() + "/disable", "{}");
            Assert.assertEquals(
                get("/organizations/jenkins/pipelines/" + job.getFullName() + "/").get("disabled"),
                true
            );
            put("/organizations/jenkins/pipelines/" + job.getFullName() + "/enable", "{}");
            Assert.assertEquals(
                get("/organizations/jenkins/pipelines/" + job.getFullName() + "/").get("disabled"),
                false
            );
        }
    }

    @Test
    public void testWorkflowPipieline() throws Exception {
        Job job = j.createProject(WorkflowJob.class, "multibranch");
        try (ACLContext ctx = ACL.as(user())) {
            Assert.assertEquals(
                get("/organizations/jenkins/pipelines/" + job.getFullName() + "/").get("disabled"),
                false
            );
            put("/organizations/jenkins/pipelines/" + job.getFullName() + "/disable", "{}");
            Assert.assertEquals(
                get("/organizations/jenkins/pipelines/" + job.getFullName() + "/").get("disabled"),
                true
            );
            put("/organizations/jenkins/pipelines/" + job.getFullName() + "/enable", "{}");
            Assert.assertEquals(
                get("/organizations/jenkins/pipelines/" + job.getFullName() + "/").get("disabled"),
                false
            );
        }
    }
}
