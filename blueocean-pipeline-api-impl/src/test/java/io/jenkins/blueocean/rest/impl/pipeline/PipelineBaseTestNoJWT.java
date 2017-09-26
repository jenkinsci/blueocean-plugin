package io.jenkins.blueocean.rest.impl.pipeline;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class PipelineBaseTestNoJWT extends PipelineBase {

    @BeforeClass
    public static void enableJWT() {
        System.setProperty("BLUEOCEAN_FEATURE_JWT_AUTHENTICATION", "false");
    }

    @AfterClass
    public static void resetJWT() {
        System.clearProperty("BLUEOCEAN_FEATURE_JWT_AUTHENTICATION");
    }

}


