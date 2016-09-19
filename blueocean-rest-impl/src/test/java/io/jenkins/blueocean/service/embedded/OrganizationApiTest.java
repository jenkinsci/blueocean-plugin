package io.jenkins.blueocean.service.embedded;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Ivan Meredith
 */
public class OrganizationApiTest extends BaseTest {
    //TODO: Fix Test - see JENKINS-38320
    //@Test
    public void organizationUsers() throws UnirestException {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");

        List users = request().jwtToken(getJwtToken(j.jenkins,"alice", "alice")).get("/organizations/jenkins/users/").build(List.class);

        Assert.assertEquals(users.size(), 1);
        Assert.assertEquals(((Map)users.get(0)).get("id"), "alice");
    }
}
