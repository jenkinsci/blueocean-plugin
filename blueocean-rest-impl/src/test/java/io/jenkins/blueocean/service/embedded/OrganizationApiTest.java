package io.jenkins.blueocean.service.embedded;

import com.mashape.unirest.http.exceptions.UnirestException;
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

    @Test
    public void defaultOrganization() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");

        Map organization = request().jwtToken(getJwtToken(j.jenkins,"alice", "alice")).get("/organizations/jenkins/").build(Map.class);
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
