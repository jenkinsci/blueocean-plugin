package io.blueocean.ath;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;

/**
 * @author cliffmeyers
 */
public class ResourceResolver {

    private Class subject;

    public ResourceResolver(Class subject) {
        this.subject = subject;
    }

    public String loadResource(String path) throws IOException {
        return Resources.toString(getResourceURL(path), Charsets.UTF_8);
    }

    public URL getResourceURL(String path) throws IOException {
        return Resources.getResource(subject, subject.getSimpleName() + "/" + path);
    }
    public String loadJenkinsFile() throws IOException {
        return loadResource("Jenkinsfile");
    }

}
