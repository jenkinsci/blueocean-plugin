/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.blueocean;

import com.cloudbees.sdk.extensibility.Extension;
import com.google.common.base.Splitter;
import com.google.inject.Injector;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.LocalController;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * A winstone controller that allows specification of the http port to use.
 * <p>
 * Need this in Blue Ocean because we need to be able to control the port used in the acceptance
 * test harness when running in CI, allowing us to link docker containers together etc via
 * predictable (Vs random, as with WinstoneController) ports.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BlueOceanWinstoneController extends LocalController {

    private static final List<String> JAVA_OPTS;

    static {
        String opts = StringUtils.defaultString(System.getenv("JENKINS_JAVA_OPTS"));
        if (opts.isEmpty()) {
            JAVA_OPTS = null;
        } else {
            //Since we are only expecting opts in the form of "-Xms=XXm -Xmx=XXXm" we'll just do a simple split.
            JAVA_OPTS = Collections.unmodifiableList(
                    Splitter.onPattern("\\s+").splitToList(opts)
            );
        }
    }

    private final int httpPort;

    @Inject
    public BlueOceanWinstoneController(Injector i) {
        super(i);
        httpPort = getWinstoneHttpPort();
    }

    protected int getWinstoneHttpPort() {
        String httpPortEnv = System.getenv("httpPort");
        if (httpPortEnv != null) {
            return Integer.parseInt(httpPortEnv);
        }

        try {
            try (ServerSocket serverSocket = new ServerSocket(0)){
                return serverSocket.getLocalPort();
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }



    @Override
    public ProcessInputStream startProcess() throws IOException {
        File javaHome = getJavaHome();
        String java = javaHome == null ? "java" : String.format("%s/bin/java",javaHome.getAbsolutePath());
        CommandBuilder cb = new CommandBuilder(java);
        if(JAVA_OPTS != null && !JAVA_OPTS.isEmpty()) {
            cb.addAll(JAVA_OPTS);
        }
        cb.add(
                "-Duser.language=en",
                "-jar", war,
                "--ajp13Port=-1",
                "--httpPort=" + httpPort);
        cb.env.putAll(commonLaunchEnv());
        System.out.println("Starting Jenkins: " + cb.toString());
        return cb.popen();
    }

    @Override
    public URL getUrl() {
        try {
            return new URL(String.format("http://" + getSutHostName() + ":%s/",httpPort));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return getUrl().toExternalForm();
    }

    @Extension
    public static class FactoryImpl extends LocalController.LocalFactoryImpl {
        @Inject Injector i;

        @Override
        public String getId() {
            return "winstone";
        }

        @Override
        public JenkinsController create() {
            return i.getInstance(BlueOceanWinstoneController.class);
        }
    }
}
