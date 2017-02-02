package io.jenkins.blueocean.blueocean_github_pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.impl.pipeline.scm.Scm;
import jenkins.model.Jenkins;
import org.acegisecurity.adapters.PrincipalAcegiUserToken;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetails;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class GithubApiTest extends PipelineBaseTest{

    //How to test with personal access token? Tested locally but need some test github account
    // Disabled for now till we have such test account
//    @Test
    public void validateToken() throws IOException, UnirestException {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        hudson.model.User bob = j.jenkins.getUser("bob");

        bob.setFullName("Bob Smith");
        bob.addProperty(new Mailer.UserProperty("bob@jenkins-ci.org"));

        //check credentialId of this SCM, should be null
        Map r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .get("/organizations/jenkins/scm/github/")
                .build(Map.class);

        Assert.assertNull(r.get("credentialId"));


        r = new RequestBuilder(baseUrl)
                .data(ImmutableMap.of("accessToken", "..."))
                .status(200)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .put("/organizations/jenkins/scm/github/validate/")
                .build(Map.class);

        Assert.assertEquals("github-bob", r.get("credentialId"));

        //now that there is github credentials setup, calling scm api to get credential should simply return that.
        r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .get("/organizations/jenkins/scm/github/")
                .build(Map.class);
        Assert.assertEquals("github-bob", r.get("credentialId"));
    }

    //@Test
    public void getOrganizationsAndRepositories() throws Exception{
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        hudson.model.User bob = j.jenkins.getUser("bob");

        bob.setFullName("Bob Smith");
        bob.addProperty(new Mailer.UserProperty("bob@jenkins-ci.org"));

        UserDetails d = Jenkins.getInstance().getSecurityRealm().loadUserByUsername(bob.getId());
        SecurityContextHolder.getContext().setAuthentication(new PrincipalAcegiUserToken(bob.getId(),bob.getId(),bob.getId(), d.getAuthorities(), bob.getId()));


        //check credentialId of this SCM, should be null
        Map r = new RequestBuilder(baseUrl)
                .data(ImmutableMap.of("accessToken", "..."))
                .status(200)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .put("/organizations/jenkins/scm/github/validate/")
                .build(Map.class);


        Assert.assertEquals("github-bob",r.get("credentialId"));
        String credentialId = (String) r.get("credentialId");


        List l  = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .get("/organizations/jenkins/scm/github/organizations/?credentialId="+credentialId)
                .header(Scm.X_CREDENTIAL_ID, credentialId+"sdsdsd") //it must be ignored as credentialId query parameter overrides it.
                .build(List.class);


        Map resp  = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .get("/organizations/jenkins/scm/github/organizations/CloudBees-community/repositories/?credentialId="+credentialId+"&pageSize=10&pageNumber=3")
                .header(Scm.X_CREDENTIAL_ID, credentialId+"sdsdsd") //it must be ignored as credentialId query parameter overrides it.
                .build(Map.class);

     resp  = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .get("/organizations/jenkins/scm/github/organizations/CloudBees-community/repositories/game-of-life/?credentialId="+credentialId)
                .header(Scm.X_CREDENTIAL_ID, credentialId+"sdsdsd") //it must be ignored as credentialId query parameter overrides it.
                .build(Map.class);


        resp  = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .get("/organizations/jenkins/scm/github/organizations/vivek/repositories/stapler/?credentialId="+credentialId)
                .header(Scm.X_CREDENTIAL_ID, credentialId+"sdsdsd") //it must be ignored as credentialId query parameter overrides it.
                .build(Map.class);


        //TODO: add more tests once there is test githuhb account
    }

    @Test
    public void shouldFailOnInvalidCredentials() throws Exception{
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        hudson.model.User bob = j.jenkins.getUser("bob");

        bob.setFullName("Bob Smith");
        bob.addProperty(new Mailer.UserProperty("bob@jenkins-ci.org"));

        UserDetails d = Jenkins.getInstance().getSecurityRealm().loadUserByUsername(bob.getId());
        SecurityContextHolder.getContext().setAuthentication(new PrincipalAcegiUserToken(bob.getId(),bob.getId(),bob.getId(), d.getAuthorities(), bob.getId()));

        Map<String,Object> pipeline = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "jenkinsci",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("stapler")), "credentialId", "abcd")
                ), 400);

    }

//    @Test
    public void shouldSucceedWithValidCredential() throws Exception{
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        hudson.model.User bob = j.jenkins.getUser("bob");

        bob.setFullName("Bob Smith");
        bob.addProperty(new Mailer.UserProperty("bob@jenkins-ci.org"));

        UserDetails d = Jenkins.getInstance().getSecurityRealm().loadUserByUsername(bob.getId());
        SecurityContextHolder.getContext().setAuthentication(new PrincipalAcegiUserToken(bob.getId(),bob.getId(),bob.getId(), d.getAuthorities(), bob.getId()));


        //check credentialId of this SCM, should be null
        Map r = new RequestBuilder(baseUrl)
                .data(ImmutableMap.of("accessToken", "..."))
                .status(200)
                .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
                .put("/organizations/jenkins/scm/github/validate/")
                .build(Map.class);


        Assert.assertEquals("github-bob",r.get("credentialId"));


        Map<String,Object> pipeline = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "jenkinsci",
                        "$class", "io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("config",
                                ImmutableMap.of("repos", ImmutableList.of("stapler")), "credentialId", "github-bob")
                ), 201);

        Assert.assertEquals("jenkinsci", pipeline.get("name"));
        Assert.assertEquals("io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder", pipeline.get("_class"));
    }
}
