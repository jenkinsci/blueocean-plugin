package io.jenkins.blueocean;

import com.github.eirslett.maven.plugins.frontend.lib.TaskRunnerException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class AllTest extends BOJUnitTest {
    @Test
    public void javaTest() throws IOException, InterruptedException {
        File wd = new File("../../");
        String[] cmd = {"mvn", "test", "-B", "-Dprofile=all"};
        ProcessBuilder processBuilder = new ProcessBuilder(cmd)
                .directory(wd)
                .inheritIO();

        Process process = processBuilder.start();
        Assert.assertEquals("exit code", 0, process.waitFor());
    }
}
