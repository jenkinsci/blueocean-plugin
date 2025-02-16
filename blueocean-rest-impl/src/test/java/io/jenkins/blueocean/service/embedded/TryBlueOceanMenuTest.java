package io.jenkins.blueocean.service.embedded;

import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Vivek Pandey
 */
public class TryBlueOceanMenuTest extends BaseTest{
    @Test
    public void testOpenBlueOcean() throws IOException, SAXException {
        JenkinsRule.WebClient webClient = j.createWebClient();
        HtmlPage page = webClient.getPage(j.getInstance());
        HtmlAnchor anchor = page.getAnchorByHref("/jenkins/blue/organizations/jenkins/pipelines/");
        Assert.assertEquals(Messages.BlueOceanUrlAction_DisplayName(), anchor.getTextContent());
        assertThat(anchor.getAttribute("class"), containsString("task-link"));
    }
}
