package io.jenkins.blueocean.service.embedded.rest;

import hudson.model.Run;
import io.jenkins.blueocean.rest.hal.Link;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
@PrepareForTest({Run.class,Link.class})
public class ArtifactImplTest {

    @Test
    public void findUniqueArtifactsWithSameName() throws IllegalAccessException {
        //mock artifacts
        Run.Artifact artifact1 = mock(Run.Artifact.class);
        Run.Artifact artifact2 = mock(Run.Artifact.class);
        //artifact1 mocks
        when(artifact1.getFileName()).thenReturn("test-suite.log");
        MemberModifier.field(Run.Artifact.class, "relativePath").set(artifact1, "path1/test-suite.log");
        when(artifact1.getHref()).thenReturn("path1/test-suite.log");
        //artifact2 mocks
        when(artifact2.getFileName()).thenReturn("test-suite.log");
        MemberModifier.field(Run.Artifact.class, "relativePath").set(artifact2, "path2/test-suite.log");
        when(artifact2.getHref()).thenReturn("path2/test-suite.log");
        //list of artifacts
        ArrayList artifactList = new ArrayList();
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
