package io.jenkins.blueocean.service.embedded;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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
        HtmlAnchor anchor = page.getAnchorByText(Messages.BlueOceanUrlAction_DisplayName());
        Assert.assertEquals("/jenkins/blue/organizations/jenkins/pipelines/", anchor.getHrefAttribute());
        assertThat(anchor.getAttribute("class"), containsString("task-link"));
    }
}
