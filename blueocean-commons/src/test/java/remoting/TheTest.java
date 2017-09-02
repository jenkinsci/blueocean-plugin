package remoting;

import hudson.remoting.Channel;
import hudson.remoting.Which;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class TheTest {
    @Rule
    public final JenkinsRule j = new JenkinsRule();

    /**
     * Ensures that the unit test is indeed loading overridden remoting
     */
    @Test
    public void test() throws Exception {
        String version = "3.12-SNAPSHOT";

             // see which jar file we are using?
        try (JarFile jar = new JarFile(Which.jarFile(Channel.class))) {
            ZipEntry e = jar.getEntry("META-INF/MANIFEST.MF");
            try (InputStream is = jar.getInputStream(e)) {
                Attributes mf = new Manifest(is).getMainAttributes();
                assertThat(mf.getValue("Version"),is(version));
            }
        }

        // see which plugin is installed?
        assertThat(trimSnapshot(j.jenkins.getPlugin("remoting").getWrapper().getVersion()), is(version));
    }

    /**
     * For version number like "1.0-SNAPSHOT (private-921e74fb-kohsuke)" trim off the whitespace and onward
     */
    private String trimSnapshot(String v) {
        return v.split(" ")[0];
    }
}

