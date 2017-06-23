package io.jenkins.blueocean.service.embedded;

import hudson.tasks.Mailer;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.OrganizationRoute;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Ivan Meredith
 */
public class OrganizationApiTest extends BaseTest {
    @Test
    public void organizationUsers() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@example.com"));

        List users = request().authAlice().get("/organizations/jenkins/users/").build(List.class);

        Assert.assertEquals(users.size(), 1);
        Map aliceMap = (Map) users.get(0);
        Assert.assertEquals(aliceMap.get("id"), "alice");
        Assert.assertEquals(aliceMap.get("fullName"), "Alice Cooper");
        Assert.assertEquals(aliceMap.get("email"), "alice@example.com");
    }

    @Test
    public void defaultOrganization() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");

        Map organization = request().authAlice().get("/organizations/jenkins/").build(Map.class);
        assertEquals("Jenkins", organization.get("displayName"));
        assertEquals("jenkins", organization.get("name"));
    }


    @TestExtension("customOrganizationRoute")
    public static class XyzRoute implements OrganizationRoute{

        @Override
        public String getUrlName() {
            return "xyz";
        }

        @WebMethod(name = "value")
        @TreeResponse
        public Xyz getValue(){
            return new Xyz();
        }

        @ExportedBean
        public static class Xyz{
            @Exported
            public String getName(){
                return "hello";
            }
        }
    }

    @Test
    public void customOrganizationRoute() throws Exception{
        get("/organizations/jenkins/xyz/value/");
    }
}
