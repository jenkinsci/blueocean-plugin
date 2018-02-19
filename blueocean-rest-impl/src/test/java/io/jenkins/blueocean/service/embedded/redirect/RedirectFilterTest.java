package io.jenkins.blueocean.service.embedded.redirect;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import io.jenkins.blueocean.service.embedded.BaseTest;
import jenkins.model.GlobalConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class RedirectFilterTest extends BaseTest {
    @Before
    public void setup() {
        Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom()
            .disableRedirectHandling()
            .build());
    }

    @After
    public void after() {
        Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom()
            .build());
    }
    @Test
    public void testRedirect() throws UnirestException, IOException {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User user = User.get("alice");

        DefaultUserInterfaceGlobalConfiguration globalConfiguration = GlobalConfiguration.all().get(DefaultUserInterfaceGlobalConfiguration.class);
        Assert.assertNotNull(globalConfiguration);
        Assert.assertNull(globalConfiguration.getInterfaceId());

        HttpResponse<String> response = Unirest.get(j.jenkins.getRootUrl())
            .basicAuth("alice", "alice")
            .asString();

        Assert.assertEquals(200, response.getStatus());


        globalConfiguration.setInterfaceId("classic");
        globalConfiguration.save();

        response = Unirest.get(j.jenkins.getRootUrl())
            .basicAuth("alice", "alice")
            .asString();

        Assert.assertEquals(200, response.getStatus());

        user.addProperty(new DefaultUserInterfaceUserProperty("system"));
        user.save();
        response = Unirest.get(j.jenkins.getRootUrl())
            .basicAuth("alice", "alice")
            .asString();

        Assert.assertEquals(200, response.getStatus());

        user.addProperty(new DefaultUserInterfaceUserProperty("classic"));
        user.save();
        response = Unirest.get(j.jenkins.getRootUrl())
            .basicAuth("alice", "alice")
            .asString();

        Assert.assertEquals(200, response.getStatus());

        user.addProperty(new DefaultUserInterfaceUserProperty("blueocean"));
        user.save();
        response = Unirest.get(j.jenkins.getRootUrl())
            .basicAuth("alice", "alice")
            .asString();

        Assert.assertEquals(302, response.getStatus());

        globalConfiguration.setInterfaceId("blueocean");
        globalConfiguration.save();

        user.addProperty(new DefaultUserInterfaceUserProperty("system"));
        user.save();
        response = Unirest.get(j.jenkins.getRootUrl())
            .basicAuth("alice", "alice")
            .asString();

        Assert.assertEquals(302, response.getStatus());

        user.addProperty(new DefaultUserInterfaceUserProperty("classic"));
        user.save();
        response = Unirest.get(j.jenkins.getRootUrl())
            .basicAuth("alice", "alice")
            .asString();

        Assert.assertEquals(200, response.getStatus());

        user.addProperty(new DefaultUserInterfaceUserProperty("blueocean"));
        user.save();
        response = Unirest.get(j.jenkins.getRootUrl())
            .basicAuth("alice", "alice")
            .asString();

        Assert.assertEquals(302, response.getStatus());
    }

    @Test
    public void testOverride() throws UnirestException {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User user = User.get("alice");

        DefaultUserInterfaceGlobalConfiguration globalConfiguration = GlobalConfiguration.all().get(DefaultUserInterfaceGlobalConfiguration.class);
        Assert.assertNotNull(globalConfiguration);
        Assert.assertNull(globalConfiguration.getInterfaceId());


        globalConfiguration.setInterfaceId("blueocean");
        globalConfiguration.save();

        HttpResponse<String> response = Unirest.get(j.jenkins.getRootUrl()+"?noDefaultRedirect=true")
            .basicAuth("alice", "alice")
            .asString();

        Assert.assertEquals(200, response.getStatus());

    }
}
