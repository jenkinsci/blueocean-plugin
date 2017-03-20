/*
 * The MIT License
 *
 * Copyright 2015 CloudBees, Inc.
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

package io.jenkins.blueocean.scm;

import hudson.triggers.SCMTrigger;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;

/**
 * TODO: Move BlueOceanWebURLBuilderTest to somewhere more appropriate.
 * That then allows us to remove these scm classes (copied from pipeline-api-impl tests)
 */

public abstract class AbstractSampleRepoRule extends ExternalResource {

    public static void run(boolean probing, File cwd, String... cmds) throws Exception {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmds);
            try {
                ProcessBuilder.class.getMethod("inheritIO").invoke(pb);
            } catch (NoSuchMethodException x) {
                // TODO remove when Java 7+
            }
            int r = pb.directory(cwd).start().waitFor();
            String message = Arrays.toString(cmds) + " failed with error code";
            if (probing) {
                Assume.assumeThat(message, r, is(0));
            } else {
                Assert.assertThat(message, r, is(0));
            }
        } catch (Exception x) {
            if (probing) {
                Assume.assumeNoException(Arrays.toString(cmds) + " failed with exception (required tooling not installed?)", x);
            } else {
                throw x;
            }
        }
    }

    protected final TemporaryFolder tmp;

    protected AbstractSampleRepoRule() {
        this.tmp = new TemporaryFolder();
    }

    @Override protected void before() throws Throwable {
        tmp.create();
    }

    @Override protected void after() {
        tmp.delete();
    }

    /** Otherwise {@link JenkinsRule#waitUntilNoActivity()} is ineffective when we have just pinged a commit notification endpoint. */
    protected final void synchronousPolling(JenkinsRule r) {
        r.jenkins.getDescriptorByType(SCMTrigger.DescriptorImpl.class).synchronousPolling = true;
    }

}
