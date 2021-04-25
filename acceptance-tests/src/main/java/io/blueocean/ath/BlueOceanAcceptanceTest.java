package io.blueocean.ath;

import java.io.IOException;
import java.net.URL;

public abstract class BlueOceanAcceptanceTest {

    private ResourceResolver resources = new ResourceResolver(getClass());

    public String loadResource(String path) throws IOException {
        return resources.loadResource(path);
    }

    public URL getResourceURL(String path) throws IOException {
        return resources.getResourceURL(path);
    }
    public String loadJenkinsFile() throws IOException {
        return resources.loadJenkinsFile();
    }

}
