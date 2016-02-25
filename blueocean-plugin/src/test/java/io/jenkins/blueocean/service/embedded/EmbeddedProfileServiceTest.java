package io.jenkins.blueocean.service.embedded;

import io.jenkins.blueocean.rest.sandbox.BOOrganization;
import io.jenkins.blueocean.service.embedded.rest.OrganizationContainerImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import javax.inject.Inject;

/**
 * @author Vivek Pandey
 */
public class EmbeddedProfileServiceTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Inject
    public OrganizationContainerImpl orgContainer;

    @Before
    public void before(){
        j.jenkins.getInjector().injectMembers(this);
    }

    @Test
    public void getOrganizationTest(){
        BOOrganization o = orgContainer.get("jenkins");
        Assert.assertEquals(o.getName(), "jenkins");
    }
}
