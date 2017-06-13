package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.model.User;
import hudson.tasks.Mailer;
import hudson.tasks.UserAvatarResolver;
import io.jenkins.blueocean.service.embedded.rest.UserImpl;
import jenkins.model.Jenkins;
import org.acegisecurity.adapters.PrincipalAcegiUserToken;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetails;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Vivek Pandey
 */
public class ProfileApiTest extends BaseTest{
    @Test
    public void getUserTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");
        get("/users/", List.class);
        Map response = get("/users/"+system.getId());
        assertEquals(system.getId(), response.get("id"));
        assertEquals(system.getFullName(), response.get("fullName"));
        assertEquals("http://avatar.example/i/img.png", response.get("avatar"));
    }

    //XXX: There is no method on User API to respond to POST or PUT or PATH. Since there are other tests that
    // does POST, PUT for successful case, its ok to disable them.
    //UX-159
//    @Test
    public void postCrumbTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");
        Map response = post("/users/"+system.getId()+"/", Collections.emptyMap());
        assertEquals(system.getId(), response.get("id"));
        assertEquals(system.getFullName(), response.get("fullName"));
    }

    //UX-159
    @Test
    public void postCrumbFailTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");

        post("/users/"+system.getId()+"/", "", "text/plain", 403);
    }

    //UX-159
    //@Test
    public void putMimeTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");
        Map response = put("/users/"+system.getId()+"/", Collections.emptyMap());
        assertEquals(system.getId(), response.get("id"));
        assertEquals(system.getFullName(), response.get("fullName"));
    }

    @Test
    public void putMimeFailTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");
        put("/users/"+system.getId(), "","text/plain", 415);
    }

    //UX-159
//    @Test
    public void patchMimeTest() throws Exception {
        User system = j.jenkins.getUser("SYSTEM");

        Map response = patch("/users/"+system.getId()+"/", Collections.emptyMap());
        assertEquals(system.getId(), response.get("id"));
        assertEquals(system.getFullName(), response.get("fullName"));
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
        assertEquals(alice.getId(), response.get("id"));
        assertEquals(alice.getFullName(), response.get("fullName"));
        Assert.assertNull(response.get("email"));

        //make a request on bob's behalf to get alice's user details, should get null email
        Map r = new RequestBuilder(baseUrl)
            .status(200)
            .auth("bob", "bob")
            .get("/users/"+alice.getId()).build(Map.class);

        assertEquals(alice.getId(), r.get("id"));
        assertEquals(alice.getFullName(), r.get("fullName"));
        Assert.assertTrue(bob.hasPermission(Jenkins.ADMINISTER));
        //bob is admin so can see alice email
        assertEquals("alice@jenkins-ci.org",r.get("email"));

        r = new RequestBuilder(baseUrl)
            .status(200)
            .authAlice()
            .get("/users/"+alice.getId()).build(Map.class);

        assertEquals(alice.getId(), r.get("id"));
        assertEquals(alice.getFullName(), r.get("fullName"));
        assertEquals("alice@jenkins-ci.org",r.get("email"));
    }

    @Test
    public void createUserFavouriteTest() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User user = j.jenkins.getUser("alice");
        user.setFullName("Alice Cooper");

        Project p = j.createFreeStyleProject("pipeline1");

      //  String token = getJwtToken(j.jenkins,"alice", "alice");
        Map map = new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/pipeline1/favorite")
            .authAlice()
            .data(ImmutableMap.of("favorite", true))
            .build(Map.class);

        validatePipeline(p, (Map) map.get("item"));
        List l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .authAlice()
            .build(List.class);

        assertEquals(1, l.size());
        Map pipeline = (Map)((Map)l.get(0)).get("item");

        validatePipeline(p, pipeline);

        String href = getHrefFromLinks((Map)l.get(0),"self");
        assertEquals("/blue/rest/organizations/jenkins/pipelines/pipeline1/favorite/", href);
        map = new RequestBuilder(baseUrl)
            .put(href.substring("/blue/rest".length()))
            .authAlice()
            .data(ImmutableMap.of("favorite", false))
            .build(Map.class);

        validatePipeline(p, (Map) map.get("item"));

        l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .authAlice()
            .build(List.class);

        assertEquals(0, l.size());

        new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .auth("bob", "bob")
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

        //String token = getJwtToken(j.jenkins,"alice", "alice");
        Map map = new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/folder1/pipelines/pipeline1/favorite/")
            .authAlice()
            .data(ImmutableMap.of("favorite", true))
            .build(Map.class);

        validatePipeline(p, (Map) map.get("item"));
        List l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .authAlice()
            .build(List.class);

        assertEquals(1, l.size());
        Map pipeline = (Map)((Map)l.get(0)).get("item");

        validatePipeline(p, pipeline);

        String href = getHrefFromLinks((Map)l.get(0),"self");

        assertEquals("/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/pipeline1/favorite/", href);

        map = new RequestBuilder(baseUrl)
            .put(href.substring("/blue/rest".length()))
            .authAlice()
            .data(ImmutableMap.of("favorite", false))
            .build(Map.class);

        validatePipeline(p, (Map) map.get("item"));

        l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .authAlice()
            .build(List.class);

        assertEquals(0, l.size());


        new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/folder1/favorite/")
            .authAlice()
            .data(ImmutableMap.of("favorite", true))
            .status(405)
            .build(Map.class);

        new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/folder1/favorite/")
            .authAlice()
            .data(ImmutableMap.of("favorite", false))
            .status(405)
            .build(Map.class);

        l = new RequestBuilder(baseUrl)
            .get("/users/"+user.getId()+"/favorites/")
            .authAlice()
            .build(List.class);

        assertEquals(0, l.size());

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

        // String token = getJwtToken(j.jenkins,"alice", "alice");

        Map u = new RequestBuilder(baseUrl)
            .get("/organizations/jenkins/user/")
            .authAlice()
            .status(200)
            .build(Map.class);

        assertEquals(user.getFullName(), u.get("fullName"));
        assertEquals("alice@jenkins-ci.org", u.get("email"));
        assertEquals(user.getId(), u.get("id"));
        Map permission = (Map) u.get("permission");
        assertNotNull(permission);
        assertTrue((Boolean) permission.get("administrator"));
        Map pipelinePerm = (Map) permission.get("pipeline");
        assertEquals(true, pipelinePerm.get("start"));
        assertEquals(true, pipelinePerm.get("create"));
        assertEquals(true, pipelinePerm.get("read"));
        assertEquals(true, pipelinePerm.get("stop"));
        assertEquals(true, pipelinePerm.get("configure"));

        Map credentialPerm = (Map) permission.get("credential");
        assertEquals(true, credentialPerm.get("create"));
        assertEquals(true, credentialPerm.get("view"));
        assertEquals(true, credentialPerm.get("update"));
        assertEquals(true, credentialPerm.get("manageDomains"));
        assertEquals(true, credentialPerm.get("delete"));
    }

    @Test
    public void testPermissionOfOtherUser() throws IOException {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        hudson.model.User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));


        hudson.model.User bob = j.jenkins.getUser("bob");
        bob.setFullName("Bob Cooper");
        bob.addProperty(new Mailer.UserProperty("bob@jenkins-ci.org"));

        UserDetails d = Jenkins.getInstance().getSecurityRealm().loadUserByUsername(bob.getId());

        SecurityContextHolder.getContext().setAuthentication(new PrincipalAcegiUserToken(bob.getId(),bob.getId(),bob.getId(), d.getAuthorities(), bob.getId()));

        Assert.assertNull(new UserImpl(alice).getPermission());
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
            .status(404)
            .build(Map.class); //sends jwt token for anonymous user
    }

    @Ignore
    @Test
    public void badTokenTest1() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        new RequestBuilder(baseUrl)
            .get("/organizations/jenkins/user/")
            .jwtToken("")
            .status(401)
            .build(Map.class);

    }

    @Ignore
    @Test
    public void badTokenTest2() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        new RequestBuilder(baseUrl)
            .get("/organizations/jenkins/user/")
            .jwtToken("aasasasas")
            .status(401)
            .build(Map.class);    }


    @Test
    public void userCurrentTest() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        SecurityContextHolder.getContext().setAuthentication(j.jenkins.ANONYMOUS);

        Assert.assertNull(User.current());

        List<Map> l = new RequestBuilder(baseUrl)
            .get("/organizations/jenkins/pipelines/")
            .authAlice()
            .build(List.class);

        assertEquals(0, l.size());
        Assert.assertNull(User.current());
    }


    @TestExtension
    public static class TestUserAvatarResolver extends UserAvatarResolver {
        @Override
        public String findAvatarFor(User u, int width, int height) {
            return "http://avatar.example/i/img.png";
        }
    }
}
