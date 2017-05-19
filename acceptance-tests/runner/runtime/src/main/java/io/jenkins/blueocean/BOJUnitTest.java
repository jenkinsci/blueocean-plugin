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

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.junit.Before;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class BOJUnitTest extends AbstractJUnitTest {
    
    @Before
    public void doInit() throws IOException {
        FileWriter fileWriter = new FileWriter("../.blueocean-ath-jenkins-url");
        fileWriter.write(getJenkinsUrl());
        fileWriter.flush();
        fileWriter.close();
        new FileWriter("./target/.jenkins_test").close();
    }

    protected String getJenkinsUrl() {
        String jenkinsUrl = jenkins.url.getProtocol() + "://" + jenkins.url.getHost() + ":" + jenkins.url.getPort();
        String host = System.getenv("blueoceanHost");

        if (host != null) {
            host = host.trim();
            jenkinsUrl = jenkinsUrl.replace("127.0.0.1", host);
            jenkinsUrl = jenkinsUrl.replace("localhost", host);
        } else {
            try {
                String ip = InetAddress.getLocalHost().getHostAddress();
                jenkinsUrl = jenkinsUrl.replace("127.0.0.1", ip);
                jenkinsUrl = jenkinsUrl.replace("localhost", ip);

            } catch (UnknownHostException e) {

            }
        }

        return jenkinsUrl;
    }
}
