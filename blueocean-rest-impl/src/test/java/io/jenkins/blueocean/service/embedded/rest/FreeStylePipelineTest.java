package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.util.RunList;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.hal.Links;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Job.class, FreeStyleProject.class, FreeStyleBuild.class })
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class FreeStylePipelineTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    @Issue("JENKINS-51716")
    public void findNonNumericRun() throws Exception {
        FreeStyleProject freestyle = Mockito.spy(j.createProject(FreeStyleProject.class, "freestyle"));

        FreeStyleBuild build1 = Mockito.mock(FreeStyleBuild.class);
        FreeStyleBuild build2 = Mockito.mock(FreeStyleBuild.class);

        Mockito.when(build1.getId()).thenReturn("build1");
        Mockito.when(build1.getParent()).thenReturn(freestyle);
        Mockito.when(build1.getNextBuild()).thenReturn(build2);

        Mockito.when(build2.getId()).thenReturn("build2");
        Mockito.when(build2.getParent()).thenReturn(freestyle);
        Mockito.when(build2.getPreviousBuild()).thenReturn(build1);

        RunList<FreeStyleBuild> runs = RunList.fromRuns(Arrays.asList(build1, build2));
        Mockito.doReturn(runs).when(freestyle).getBuilds();
        Mockito.doReturn(build2).when(freestyle).getLastBuild();

        FreeStylePipeline freeStylePipeline = (FreeStylePipeline) BluePipelineFactory.resolve(freestyle);
        assertNotNull(freeStylePipeline);
        BlueRun blueRun = freeStylePipeline.getLatestRun();
        assertNotNull(blueRun);
        Links links = blueRun.getLinks();
        assertNotNull(links);
        assertNotNull(links.get("self"));
    }

    @Test
    public void findModernRun() throws Exception {
        FreeStyleProject freestyle = Mockito.spy(j.createProject(FreeStyleProject.class, "freestyle"));

        FreeStyleBuild build1 = Mockito.mock(FreeStyleBuild.class);
        FreeStyleBuild build2 = Mockito.mock(FreeStyleBuild.class);

        Mockito.when(build1.getParent()).thenReturn(freestyle);
        Mockito.when(build1.getNextBuild()).thenReturn(build2);

        Mockito.when(build2.getParent()).thenReturn(freestyle);
        Mockito.when(build2.getPreviousBuild()).thenReturn(build1);

        RunList<FreeStyleBuild> runs = RunList.fromRuns(Arrays.asList(build1, build2));
        Mockito.doReturn(runs).when(freestyle).getBuilds();
        Mockito.doReturn(build2).when(freestyle).getLastBuild();

        FreeStylePipeline freeStylePipeline = (FreeStylePipeline) BluePipelineFactory.resolve(freestyle);
        assertNotNull(freeStylePipeline);
        BlueRun blueRun = freeStylePipeline.getLatestRun();
        assertNotNull(blueRun);
        Links links = blueRun.getLinks();
        assertNotNull(links);
        assertNotNull(links.get("self"));
    }

}
