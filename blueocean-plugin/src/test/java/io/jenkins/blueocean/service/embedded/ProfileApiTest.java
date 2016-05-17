package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import hudson.model.Project;
import hudson.model.User;
import hudson.tasks.Mailer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class ProfileApiTest extends BaseTest{
    @Test
    public void getUserTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");
        Map response = get("/users/"+system.getId());
        Assert.assertEquals(system.getId(), response.get("id"));
        Assert.assertEquals(system.getFullName(), response.get("fullName"));
    }

    //UX-159
    @Test
    public void postCrumbTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");
        Map response = post("/users/"+system.getId()+"/", Collections.emptyMap());
        Assert.assertEquals(system.getId(), response.get("id"));
        Assert.assertEquals(system.getFullName(), response.get("fullName"));
    }

    //UX-159
    @Test
    public void postCrumbFailTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");

        post("/users/"+system.getId()+"/", "", "text/plain", 403);
    }

    //UX-159
    @Test
    public void putMimeTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");
        Map response = put("/users/"+system.getId()+"/", Collections.emptyMap());
        Assert.assertEquals(system.getId(), response.get("id"));
        Assert.assertEquals(system.getFullName(), response.get("fullName"));
    }

    @Test
    public void putMimeFailTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");
        put("/users/"+system.getId(), "","text/plain", 415);
    }

    //UX-159
    @Test
    public void patchMimeTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");

        Map response = patch("/users/"+system.getId()+"/", Collections.emptyMap());
        Assert.assertEquals(system.getId(), response.get("id"));
        Assert.assertEquals(system.getFullName(), response.get("fullName"));
    }

    @Test
    public void patchMimeFailTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");
        patch("/users/"+system.getId(), "","text/plain", 415);
    }

    @Test
    public void getUserDetailsTest() throws Exception {
        hudson.model.User user = j.jenkins.getUser("alice");
        user.setFullName("Alice Cooper");
        user.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        Map response = get("/users/"+user.getId());
        Assert.assertEquals(user.getId(), response.get("id"));
        Assert.assertEquals(user.getFullName(), response.get("fullName"));
        Assert.assertEquals("alice@jenkins-ci.org", response.get("email"));
    }

    @Test
    public void createUserFavouriteTest() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User user = j.jenkins.getUser("alice");
        user.setFullName("Alice Cooper");
        Project p = j.createFreeStyleProject("pipeline1");

        new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/pipeline1/favorite")
            .auth("alice", "alice")
            .data(ImmutableMap.of("favorite", true))
            .build(String.class);

        List l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .auth("alice","alice")
            .build(List.class);

        Assert.assertEquals(l.size(), 1);
        Assert.assertEquals(((Map)l.get(0)).get("pipeline"),"/organizations/jenkins/pipelines/pipeline1");

        new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .auth("bob","bob")
            .status(403)
            .build(String.class);

    }


    @Test
    public void getOrganizationTest(){
        Map response = get("/organizations/jenkins");
        Assert.assertEquals("jenkins", response.get("name"));
    }

    @Test
    public void FindUsersTest() throws Exception {
        List<String> names = ImmutableList.of("alice", "bob");
        j.jenkins.getUser(names.get(0));
        j.jenkins.getUser(names.get(1));

        List response = get("/search?q=type:user;organization:jenkins", List.class);

        for(Object r: response){
            Map org = (Map) r;
            Assert.assertTrue(names.contains((String)org.get("id")));
            Assert.assertTrue(names.contains((String)org.get("fullName")));
        }

    }

}
