package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableList;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

/**
 * @author Vivek Pandey
 */
public class EmbeddedProfileServiceRestTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void before() {
        RestAssured.baseURI = j.jenkins.getRootUrl()+"bo/rest";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void getUserTest() throws Exception {

        RestAssured.given().log().all().get("/users/"+j.jenkins.getUser("SYSTEM").getId())
            .then().log().all()
            .statusCode(200)
            .body("user.id", Matchers.equalTo(j.jenkins.getUser("SYSTEM").getId()))
            .body("user.name", Matchers.equalTo(j.jenkins.getUser("SYSTEM").getFullName()));
    }

    @Test
    public void getUserDetailsTest() throws Exception {
        hudson.model.User user = j.jenkins.getUser("alice");

        RestAssured.given().log().all().get("/users/"+user.getId()+"?details=true")
            .then().log().all()
            .statusCode(200)
            .body("user.id", Matchers.equalTo(user.getId()))
            .body("user.name", Matchers.equalTo(user.getFullName()))
            .body("user.email", Matchers.equalTo("none"));
    }

    @Test
    public void getOrganizationTest(){
        RestAssured.given().log().all().get("/organizations/jenkins")
            .then().log().all()
            .statusCode(200)
            .body("name", Matchers.equalTo("jenkins"));
    }

    @Test
    public void FindUsersTest() throws Exception {
        List<String> names = ImmutableList.of("alice", "bob");
        j.jenkins.getUser(names.get(0));
        j.jenkins.getUser(names.get(1));

        Response response = RestAssured.given().log().all().get("/search?q=type:user;organization:jenkins");

        response.then().log().all().statusCode(200);

        Assert.assertTrue(names.contains((String)response.path("users[0].id")));
        Assert.assertTrue(names.contains((String)response.path("users[0].name")));
        Assert.assertTrue(names.contains((String)response.path("users[1].id")));
        Assert.assertTrue(names.contains((String)response.path("users[1].name")));
    }



}
