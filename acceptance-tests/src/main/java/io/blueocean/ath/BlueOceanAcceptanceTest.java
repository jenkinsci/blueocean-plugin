package io.blueocean.ath;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;

public abstract class BlueOceanAcceptanceTest {
    public String loadResource(String path) throws IOException {
        return Resources.toString(getResourceURL(path), Charsets.UTF_8);
    }

    public URL getResourceURL(String path) throws IOException {
        return Resources.getResource(this.getClass(), this.getClass().getSimpleName() + "/" + path);
    }
    public String loadJenkinsFile() throws IOException {
        return loadResource("Jenkinsfile");
    }


}
