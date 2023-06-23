package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;
import io.jenkins.blueocean.rest.hal.Link;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArtifactImplTest {

    @Test
    public void findUniqueArtifactsWithSameName() throws IllegalAccessException, NoSuchFieldException {
        //mock artifacts
        assumeTrue("TODO in Java 12+ final cannot be removed", Runtime.version().feature() < 12);
        FieldUtils.removeFinalModifier(Run.Artifact.class.getField("relativePath"));
        Run.Artifact artifact1 = mock(Run.Artifact.class);
        Run.Artifact artifact2 = mock(Run.Artifact.class);
        //artifact1 mocks
        when(artifact1.getFileName()).thenReturn("test-suite.log");
        // FIXME with Junit5 use JUnit 5
        Whitebox.setInternalState(artifact1, "relativePath", "path1/test-suite.log");
        when(artifact1.getHref()).thenReturn("path1/test-suite.log");
        //artifact2 mocks
        when(artifact2.getFileName()).thenReturn("test-suite.log");
        Whitebox.setInternalState(artifact2, "relativePath", "path2/test-suite.log");
        when(artifact2.getHref()).thenReturn("path2/test-suite.log");
        //list of artifacts
        List artifactList = new ArrayList<>();
        artifactList.add(artifact1);
        artifactList.add(artifact2);
        //mock run
        Run run = mock(Run.class);
        when(run.getUrl()).thenReturn("job/myfolder/job/myjob/1/");
        when(run.getArtifacts()).thenReturn(artifactList);

        Link parentLink = mock(Link.class);

        ArtifactImpl a1 = new ArtifactImpl(run, artifact1, parentLink);
        ArtifactImpl a2 = new ArtifactImpl(run, artifact2, parentLink);
        assertThat(a1.getId(), is(not(a2.getId())));
    }
}
