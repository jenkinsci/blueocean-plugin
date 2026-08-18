package io.jenkins.blueocean.service.embedded;

import hudson.model.Action;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlPage;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Vivek Pandey
 */
public class TryBlueOceanMenuTest extends BaseTest{
    @Test
    public void testOpenBlueOcean() throws IOException, SAXException {
        JenkinsRule.WebClient webClient = j.createWebClient();
        HtmlPage page = webClient.getPage(j.getInstance());

        DomElement sidePanel = page.querySelector("#side-panel");
        HtmlAnchor anchor = sidePanel.getElementsByTagName("a")
            .stream()
            .filter(HtmlAnchor.class::isInstance)
            .map(HtmlAnchor.class::cast)
            .filter(a -> a.asNormalizedText().equals(Messages.BlueOceanUrlAction_DisplayName()))
            .findFirst()
            .orElseThrow();
        Assert.assertEquals("/jenkins/blue/organizations/jenkins/pipelines/", anchor.getHrefAttribute());
        assertThat(anchor.getAttribute("class"), containsString("task-link"));
    }

    @Test
    public void createForSkipsFlowNodes() {
        // A FlowNode has no Blue Ocean URL, so the factory must attach nothing to it. Guards the
        // fix for the per-FlowNode BlueOceanUrlObjectImpl/BlueOceanUrlAction allocation that, because
        // type() is ModelObject, ran for every node of every pipeline and drove the controller into
        // GC death at scale.
        FlowNode node = mock(FlowNode.class);
        Collection<? extends Action> actions = new TryBlueOceanMenu().createFor(node);
        Assert.assertTrue("Blue Ocean must not attach actions to a FlowNode", actions.isEmpty());
    }
}
