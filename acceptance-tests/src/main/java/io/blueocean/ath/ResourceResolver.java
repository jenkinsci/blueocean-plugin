package io.blueocean.ath;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author cliffmeyers
 */
public class ResourceResolver {

    private Class subject;

    public ResourceResolver(Class subject) {
        this.subject = subject;
    }

    public String loadResource(String path) throws IOException {
        return IOUtils.toString(getResourceURL(path), StandardCharsets.UTF_8);
    }

    public URL getResourceURL(String path) throws IOException {
        return subject.getResource(subject.getSimpleName() + "/" + path);
    }
    public String loadJenkinsFile() throws IOException {
        return loadResource("Jenkinsfile");
    }

}
