package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import jenkins.model.Jenkins;
import org.acegisecurity.adapters.PrincipalAcegiUserToken;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetails;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class GithubOrgFolderTest extends PipelineBaseTest {
    @Test
    public void simpleOrgTest() throws IOException, UnirestException {
        login();
        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "jenkinsci",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("stapler")))
                ))
                .build(Map.class);

        Assert.assertEquals("jenkinsci", resp.get("name"));
        Assert.assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));
    }

    @Test
    public void orgUpdateTest() throws IOException, UnirestException {
        login();

        Map resp = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .post("/organizations/jenkins/pipelines/")
                .data(ImmutableMap.of("name", "jenkinsci",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("stapler")))
                ))
                .build(Map.class);

        Assert.assertEquals("jenkinsci", resp.get("name"));
        Assert.assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", resp.get("_class"));

        new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .put("/organizations/jenkins/pipelines/jenkinsci/")
                .data(ImmutableMap.of("name", "jenkinsci",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineUpdateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("stapler")))
                ))
                .build(Map.class);
    }

    private User login() throws IOException {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        hudson.model.User bob = j.jenkins.getUser("bob");

        bob.setFullName("Bob Smith");
        bob.addProperty(new Mailer.UserProperty("bob@jenkins-ci.org"));


        UserDetails d = Jenkins.getInstance().getSecurityRealm().loadUserByUsername(bob.getId());

        SecurityContextHolder.getContext().setAuthentication(new PrincipalAcegiUserToken(bob.getId(),bob.getId(),bob.getId(), d.getAuthorities(), bob.getId()));
        return bob;
    }
}
