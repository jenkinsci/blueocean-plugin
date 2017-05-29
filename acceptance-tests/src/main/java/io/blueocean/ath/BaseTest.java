package io.blueocean.ath;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;

public abstract class BaseTest {
    public String loadResource(String path) throws IOException {
        URL url = Resources.getResource(this.getClass(), this.getClass().getSimpleName() + "/" + path);
        return Resources.toString(url, Charsets.UTF_8);
    }

    public String loadJenkinsFile() throws IOException {
        return loadResource("Jenkinsfile");
    }


}
