package io.jenkins.blueocean.service.embedded;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Ivan Meredith
 */
public class OrganizationApiTest extends BaseTest {
    @Test
    public void organizationUsers() {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");

        List users = request().authAlice().get("/organizations/jenkins/users/").build(List.class);

        Assert.assertEquals(users.size(), 1);
        Assert.assertEquals(((Map)users.get(0)).get("id"), "alice");
    }
}
