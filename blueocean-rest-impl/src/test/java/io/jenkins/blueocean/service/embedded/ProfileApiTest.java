package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.model.User;
import hudson.tasks.Mailer;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.MockFolder;

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
        get("/users/", List.class);
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

        new RequestBuilder(baseUrl)
            .contentType("text/plain")
            .status(415)
            .patch("/users/"+system.getId())
            .build(Map.class);
    }

    @Test
    public void getUserDetailsTest() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        hudson.model.User bob = j.jenkins.getUser("bob");

        bob.setFullName("Bob Smith");
        bob.addProperty(new Mailer.UserProperty("bob@jenkins-ci.org"));

        //Call is made as anonymous user, email should be null
        Map response = get("/users/"+alice.getId());
        Assert.assertEquals(alice.getId(), response.get("id"));
        Assert.assertEquals(alice.getFullName(), response.get("fullName"));
        Assert.assertNull(response.get("email"));

        //make a request on bob's behalf to get alice's user details, should get null email
        Map r = new RequestBuilder(baseUrl)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins,"bob", "bob"))
            .get("/users/"+alice.getId()).build(Map.class);

        Assert.assertEquals(alice.getId(), r.get("id"));
        Assert.assertEquals(alice.getFullName(), r.get("fullName"));
        Assert.assertTrue(bob.hasPermission(Jenkins.ADMINISTER));
        //bob is admin so can see alice email
        Assert.assertEquals("alice@jenkins-ci.org",r.get("email"));

        r = new RequestBuilder(baseUrl)
            .status(200)
            .jwtToken(getJwtToken(j.jenkins,"alice", "alice"))
            .get("/users/"+alice.getId()).build(Map.class);

        Assert.assertEquals(alice.getId(), r.get("id"));
        Assert.assertEquals(alice.getFullName(), r.get("fullName"));
        Assert.assertEquals("alice@jenkins-ci.org",r.get("email"));
    }

    @Test
    public void createUserFavouriteTest() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User user = j.jenkins.getUser("alice");
        user.setFullName("Alice Cooper");

        Project p = j.createFreeStyleProject("pipeline1");

        String token = getJwtToken(j.jenkins,"alice", "alice");
        Map map = new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/pipeline1/favorite")
            .jwtToken(token)
            .data(ImmutableMap.of("favorite", true))
            .build(Map.class);

        validatePipeline(p, (Map) map.get("item"));
        List l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .jwtToken(token)
            .build(List.class);

        Assert.assertEquals(1, l.size());
        Map pipeline = (Map)((Map)l.get(0)).get("item");

        validatePipeline(p, pipeline);

        String href = getHrefFromLinks((Map)l.get(0),"self");
        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/favorite/", href);
        map = new RequestBuilder(baseUrl)
            .put(href.substring("/blue/rest".length()))
            .jwtToken(token)
            .data(ImmutableMap.of("favorite", false))
            .build(Map.class);

        validatePipeline(p, (Map) map.get("item"));

        l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .jwtToken(token)
            .build(List.class);

        Assert.assertEquals(0, l.size());

        new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .jwtToken(getJwtToken(j.jenkins,"bob","bob"))
            .status(403)
            .build(String.class);

    }

    @Test
    public void createUserFavouriteFolderTest() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User user = j.jenkins.getUser("alice");
        user.setFullName("Alice Cooper");

        MockFolder folder1 = j.createFolder("folder1");
        Project p = folder1.createProject(FreeStyleProject.class, "pipeline1");

        String token = getJwtToken(j.jenkins,"alice", "alice");
        Map map = new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/folder1/pipelines/pipeline1/favorite/")
            .jwtToken(token)
            .data(ImmutableMap.of("favorite", true))
            .build(Map.class);

        validatePipeline(p, (Map) map.get("item"));
        List l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .jwtToken(token)
            .build(List.class);

        Assert.assertEquals(1, l.size());
        Map pipeline = (Map)((Map)l.get(0)).get("item");

        validatePipeline(p, pipeline);

        String href = getHrefFromLinks((Map)l.get(0),"self");

        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/pipeline1/favorite/", href);

        map = new RequestBuilder(baseUrl)
            .put(href.substring("/blue/rest".length()))
            .jwtToken(token)
            .data(ImmutableMap.of("favorite", false))
            .build(Map.class);

        validatePipeline(p, (Map) map.get("item"));

        l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .jwtToken(token)
            .build(List.class);

        Assert.assertEquals(0, l.size());


        map = new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/folder1/favorite/")
            .jwtToken(token)
            .data(ImmutableMap.of("favorite", true))
            .build(Map.class);

        validateFolder(folder1, (Map) map.get("item"));
        l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .jwtToken(token)
            .build(List.class);

        Assert.assertEquals(1, l.size());
        Map folder = (Map)((Map)l.get(0)).get("item");

        validateFolder(folder1, folder);

        href = getHrefFromLinks((Map)l.get(0),"self");

        Assert.assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/favorite/", href);

        map = new RequestBuilder(baseUrl)
            .put(href.substring("/blue/rest".length()))
            .jwtToken(token)
            .data(ImmutableMap.of("favorite", false))
            .build(Map.class);

        validateFolder(folder1, (Map) map.get("item"));

        l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .jwtToken(token)
            .build(List.class);

        Assert.assertEquals(0, l.size());



        new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .jwtToken(getJwtToken(j.jenkins,"bob","bob"))
            .status(403)
            .build(String.class);

    }


    @Test
    public void getOrganizationTest(){
        get("/organizations/", List.class);
//        Assert.assertEquals("jenkins", response.get("name"));
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


    @Test
    public void getAuthenticatedUser() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User user = j.jenkins.getUser("alice");
        user.setFullName("Alice Cooper");
        user.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        String token = getJwtToken(j.jenkins,"alice", "alice");

        Map u = new RequestBuilder(baseUrl)
            .get("/organizations/jenkins/user/")
            .jwtToken(token)
            .status(200)
            .build(Map.class);

        Assert.assertEquals(user.getFullName(), u.get("fullName"));
        Assert.assertEquals("alice@jenkins-ci.org", u.get("email"));
        Assert.assertEquals(user.getId(), u.get("id"));
    }


    @Test
    public void getAuthenticatedUserShouldFail() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User user = j.jenkins.getUser("alice");
        user.setFullName("Alice Cooper");
        user.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        hudson.model.User user1 = j.jenkins.getUser("bob");
        user1.setFullName("Bob Cooper");
        user1.addProperty(new Mailer.UserProperty("bob@jenkins-ci.org"));

        Map u = new RequestBuilder(baseUrl)
            .get("/organizations/jenkins/user/")
            .build(Map.class);

        Assert.assertEquals("anonymous",u.get("id"));
    }

}
