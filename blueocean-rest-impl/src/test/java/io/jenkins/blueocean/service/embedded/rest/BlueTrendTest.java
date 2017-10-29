package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import io.jenkins.blueocean.rest.factory.BlueTrendFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueTableRow;
import io.jenkins.blueocean.rest.model.BlueTrend;
import io.jenkins.blueocean.rest.model.BlueTrendContainer;
import io.jenkins.blueocean.rest.model.Container;
import io.jenkins.blueocean.service.embedded.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;

/**
 * @author cliffmeyers
 */
public class BlueTrendTest extends BaseTest {

    // dummy extension that is sorted to the end, to prove that ID collisions don't break trends
    @Extension(ordinal = -100)
    public static final BlueTrendFactory OTHER_JUNIT_TREND_FACTORY = new BlueTrendFactory() {
        @Override
        public BlueTrend getTrend(final BluePipeline pipeline, final Link parent) {
            return new BlueTrend() {
                @Override
                public String getId() {
                    return "junit";
                }

                @Override
                public String getDisplayName() {
                    return "Imposter JUnit";
                }

                @Override
                public Map<String, String> getColumns() {
                    return ImmutableMap.of();
                }

                @Override
                public Container<BlueTableRow> getRows() {
                    return new Container<BlueTableRow>() {
                        @Override
                        public Iterator<BlueTableRow> iterator() {
                            return null;
                        }

                        @Override
                        public Link getLink() {
                            return parent.rel("rows");
                        }

                        @Override
                        public BlueTableRow get(String name) {
                            return new BlueTableRow() {
                                @Override
                                public String getId() {
                                    return "0";
                                }
                            };
                        }
                    };
                }

                @Override
                public Link getLink() {
                    return parent.rel(getId());
                }
            };
        }
    };

    @Test
    public void testTrendsIdCollision() throws Exception {
        // verify the extension did register correctly
        ExtensionList<BlueTrendFactory> extensionList = ExtensionList.lookup(BlueTrendFactory.class);
        Assert.assertEquals(2, extensionList.size());

        Project project = j.createProject(FreeStyleProject.class, "freestyle1");
        BlueOrganization org = new OrganizationImpl("jenkins", j.jenkins);
        BluePipeline pipeline = new AbstractPipelineImpl(org, project);

        BlueTrendContainer trends = pipeline.getTrends();
        BlueTrend trend = trends.get("junit");
        Assert.assertEquals("junit", trend.getId());
        Assert.assertEquals("JUnit", trend.getDisplayName());
    }
}
