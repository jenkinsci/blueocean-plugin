package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableMap;
import hudson.model.Project;
import hudson.model.User;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author cliffmeyers
 */
public class FavoritesApiTest extends BaseTest {

    private Project createAndFavorite(String jobName, String username, String password) throws IOException {
        Project project = j.createFreeStyleProject(jobName);

        new RequestBuilder(baseUrl)
            .put("/organizations/jenkins/pipelines/"+jobName+"/favorite/")
            .auth(username, password)
            .data(ImmutableMap.of("favorite", true))
            .build(Map.class);

        return project;
    }


    @Test
    public void deleteUserFavoritesUnauthenticatedTest() {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User user = User.get("alice");

        new RequestBuilder(baseUrl)
            .delete("/users/"+user.getId()+"/favorites/")
            .status(403)
            .build(Map.class);
    }

    @Test
    public void deleteUserFavoritesUnauthorizedTest() {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User user = User.get("alice");

        new RequestBuilder(baseUrl)
            .delete("/users/"+user.getId()+"/favorites/")
            .auth("bob", "bob")
            .status(403)
            .build(Map.class);
    }

    @Test
    public void deleteUserFavoritesSuccessfulTest() throws IOException {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        String username = "alice";
        String password = "alice";

        createAndFavorite("pipeline1", username, password);
        createAndFavorite("pipeline2", username, password);

        List favorites = new RequestBuilder(baseUrl)
            .get("/users/"+username+"/favorites/")
            .auth(username, password)
            .build(List.class);

        assertEquals(2, favorites.size());

        new RequestBuilder(baseUrl)
            .delete("/users/"+username+"/favorites/")
            .auth(username, password)
            .status(204)
            .build(Map.class);

        favorites = new RequestBuilder(baseUrl)
            .get("/users/"+username+"/favorites/")
            .auth(username, password)
            .build(List.class);

        assertEquals("all favorites must be deleted",0, favorites.size());
    }

}
