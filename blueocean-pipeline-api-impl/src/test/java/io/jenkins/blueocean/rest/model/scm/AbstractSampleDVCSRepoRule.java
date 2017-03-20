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

package io.jenkins.blueocean.rest.model.scm;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractSampleDVCSRepoRule extends AbstractSampleRepoRule {

    protected File sampleRepo;

    @Override protected void before() throws Throwable {
        super.before();
        sampleRepo = tmp.newFolder();
    }

    public final void write(String rel, String text) throws IOException {
        FileUtils.writeStringToFile(new File(sampleRepo, rel), text);
    }

    @Override public final String toString() {
        return sampleRepo.getAbsolutePath();
    }

    public abstract void init() throws Exception;

    protected final void run(String tool, String... cmds) throws Exception {
        List<String> args = new ArrayList<String>();
        args.add(tool);
        args.addAll(Arrays.asList(cmds));
        run(false, sampleRepo, args.toArray(new String[args.size()]));
    }

    public final String bareUrl() throws UnsupportedEncodingException {
        return URLEncoder.encode(toString(), "UTF-8");
    }

    public final String fileUrl() {
        return sampleRepo.toURI().toString();
    }

}
