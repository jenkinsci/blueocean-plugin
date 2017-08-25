package io.jenkins.blueocean.service.embedded;

import hudson.model.User;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.commons.stapler.TreeResponse;
import io.jenkins.blueocean.rest.OrganizationRoute;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import com.mashape.unirest.http.HttpResponse;

import java.util.List;
import java.util.Map;

import hudson.Extension;
import hudson.ExtensionPoint;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.ApiRoutable;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BlueOrganizationContainer;
import io.jenkins.blueocean.rest.model.Container;

import java.util.ArrayList;
import java.util.Iterator;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class APIHeadTest extends BaseTest {
    @Test
    public void defaultCacheHeaderTest() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User alice = User.get("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@example.com"));

        RequestBuilder requestBuilder = request().authAlice().get("/organizations/jenkins/users/");

        HttpResponse<List> response = requestBuilder.execute(List.class);

        List<String> list = response.getHeaders().get("Cache-Control");

        assertThat(list.get(0), containsString("no-cache"));
        assertThat(list.get(0), containsString("no-store"));
        assertThat(list.get(0), containsString("no-transform"));
    }

    public void overrideCacheHeaderTest() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        hudson.model.User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@example.com"));

        RequestBuilder requestBuilder = request().authAlice().get("/test/");

        HttpResponse<List> response = requestBuilder.execute(List.class);

        List<String> list = response.getHeaders().get("Cache-Control");
        assertThat(list.get(0), containsString("max-age=10"));

    }

    @TestExtension("overrideCacheHeaderTest")
    public static class TestContainer extends Container<String> implements ApiRoutable, ExtensionPoint {

        @Override
        public final String getUrlName() {
            return "test";
        }

        @Override
        public String get(String name) {
            return "testString";
        }

        @Override
        @SuppressWarnings("unchecked")
        public Iterator<String> iterator() {
            StaplerResponse response = Stapler.getCurrentResponse();
            response.setHeader("Cache-Control", "max-age=10");
            return new ArrayList<String>().iterator();
        }

        @Override
        public Link getLink() {
            return ApiHead.INSTANCE().getLink().rel("test");
        }

    }

}
