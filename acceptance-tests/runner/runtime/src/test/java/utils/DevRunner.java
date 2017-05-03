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
package utils;

import io.jenkins.blueocean.BOJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.junit.Test;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DevRunner extends BOJUnitTest {
    
    @Test
    public void runAndStayRunning() throws Exception {
        System.out.println("");
        System.out.println("------------------------------------------------------------------------------------");
        System.out.println("    A clean dev instance of Jenkins is running now.");
        System.out.println("     - " + getJenkinsUrl());
        System.out.println("");
        System.out.println("    You should now be able to develop tests against this instance without");
        System.out.println("    having to constantly restart Jenkins.");
        System.out.println("");
        System.out.println("    You should be able to connect your debugger to your running Jenkins.");
        System.out.println("    instance on port 15000.");
        System.out.println("");
        System.out.println("    Open another terminal and run nightwatchjs commands to run specific tests.");
        System.out.println("    Iterate and rerun tests.");
        System.out.println("    See http://nightwatchjs.org/");
        System.out.println("");
        System.out.println("    NOTE:");
        System.out.println("        Selenium and the browser (Firefox) are running in a docker");
        System.out.println("        container that also has VNC. This allows you to connect if");
        System.out.println("        you'd like to look at the browser while the tests run.");
        System.out.println("        Simple run:");
        System.out.println("         $ open vnc://:secret@localhost:15900");
        System.out.println("");
        System.out.println("------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("ctrl-c to exit...");
        
        while(true) {
            Thread.sleep(1000);
        }
    }
}
