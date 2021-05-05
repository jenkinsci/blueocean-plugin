package io.jenkins.blueocean;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class AllTest extends BOJUnitTest {
    @Test
    public void javaTest() throws IOException, InterruptedException {
        File wd = new File("../../");
        String[] cmd = {"mvn", "test", "-D-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn","-B", "-Dprofile=all"};
        ProcessBuilder processBuilder = new ProcessBuilder(cmd)
                .directory(wd)
                .inheritIO();

        Process process = processBuilder.start();
        Assert.assertEquals("exit code", 0, process.waitFor());
    }
}
