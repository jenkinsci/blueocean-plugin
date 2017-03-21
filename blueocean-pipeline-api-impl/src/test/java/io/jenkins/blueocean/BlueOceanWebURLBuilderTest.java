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

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import io.jenkins.blueocean.rest.model.scm.GitSampleRepoRule;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BlueOceanWebURLBuilderTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Test
    public void test_freestyle() throws IOException, ExecutionException, InterruptedException {
        MockFolder folder1 = jenkinsRule.createFolder("folder1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "folder two with spaces");
        FreeStyleProject freestyleProject = folder2.createProject(FreeStyleProject.class, "freestyle with spaces");
        String blueOceanURL;

        blueOceanURL = BlueOceanWebURLBuilder.toBlueOceanURL(freestyleProject);
        assertURL("blue/organizations/jenkins/folder1%2Ffolder%20two%20with%20spaces%2Ffreestyle%20with%20spaces", blueOceanURL);

        FreeStyleBuild run = freestyleProject.scheduleBuild2(0).get();
        blueOceanURL = BlueOceanWebURLBuilder.toBlueOceanURL(run);
        assertURL("blue/organizations/jenkins/folder1%2Ffolder%20two%20with%20spaces%2Ffreestyle%20with%20spaces/detail/freestyle%20with%20spaces/1", blueOceanURL);
    }


    @Test
    public void getMultiBranchPipelineInsideFolder() throws Exception {
        MockFolder folder1 = jenkinsRule.createFolder("folder1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "folder two with spaces");
        WorkflowMultiBranchProject mp = folder2.createProject(WorkflowMultiBranchProject.class, "p");

        String blueOceanURL;

        blueOceanURL = BlueOceanWebURLBuilder.toBlueOceanURL(mp);
        assertURL("blue/organizations/jenkins/folder1%2Ffolder%20two%20with%20spaces%2Fp/branches", blueOceanURL);

        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false), new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        for (SCMSource source : mp.getSCMSources()) {
            assertEquals(mp, source.getOwner());
        }

        mp.scheduleBuild2(0).getFuture().get();
        jenkinsRule.waitUntilNoActivity();

        // All branch jobs should just resolve back to the same top level branches
        // page for the multibranch job in Blue Ocean.
        WorkflowJob masterJob = findBranchProject(mp, "master");
        blueOceanURL = BlueOceanWebURLBuilder.toBlueOceanURL(masterJob);
        assertURL("blue/organizations/jenkins/folder1%2Ffolder%20two%20with%20spaces%2Fp/branches", blueOceanURL);
        WorkflowJob featureUx1Job = findBranchProject(mp, "feature/ux-1");
        blueOceanURL = BlueOceanWebURLBuilder.toBlueOceanURL(featureUx1Job);
        assertURL("blue/organizations/jenkins/folder1%2Ffolder%20two%20with%20spaces%2Fp/branches", blueOceanURL);
        WorkflowJob feature2Job = findBranchProject(mp, "feature2");
        blueOceanURL = BlueOceanWebURLBuilder.toBlueOceanURL(feature2Job);
        assertURL("blue/organizations/jenkins/folder1%2Ffolder%20two%20with%20spaces%2Fp/branches", blueOceanURL);

        // Runs on the jobs
        blueOceanURL = BlueOceanWebURLBuilder.toBlueOceanURL(masterJob.getFirstBuild());
        assertURL("blue/organizations/jenkins/folder1%2Ffolder%20two%20with%20spaces%2Fp/detail/master/1", blueOceanURL);
        blueOceanURL = BlueOceanWebURLBuilder.toBlueOceanURL(featureUx1Job.getFirstBuild());
        assertURL("blue/organizations/jenkins/folder1%2Ffolder%20two%20with%20spaces%2Fp/detail/feature%2Fux-1/1", blueOceanURL);
    }

    private WorkflowJob findBranchProject(WorkflowMultiBranchProject mp, String name) throws Exception {
        WorkflowJob p = mp.getItem(BlueOceanWebURLBuilder.encodeURIComponent(name));
        if (p == null) {
            mp.getIndexing().writeWholeLogTo(System.out);
            fail(name + " project not found");
        }
        return p;
    }

    private void assertURL(String expected, String actual) throws IOException {
        Assert.assertEquals(expected, actual);
    }

    @Before
    public void setupScm() throws Exception {
        // create git repo
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "stage 'build'\n "+"node {echo 'Building'}\n"+
            "stage 'test'\nnode { echo 'Testing'}\n"+
            "stage 'deploy'\nnode { echo 'Deploying'}\n"
        );
        sampleRepo.write("file", "initial content");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");

        //create feature branch
        sampleRepo.git("checkout", "-b", "feature/ux-1");
        sampleRepo.write("Jenkinsfile", "echo \"branch=${env.BRANCH_NAME}\"; "+"node {" +
            "   stage ('Build'); " +
            "   echo ('Building'); " +
            "   stage ('Test'); " +
            "   echo ('Testing'); " +
            "   stage ('Deploy'); " +
            "   echo ('Deploying'); " +
            "}");
        ScriptApproval.get().approveSignature("method java.lang.String toUpperCase");
        sampleRepo.write("file", "subsequent content1");
        sampleRepo.git("commit", "--all", "--message=tweaked1");

        //create feature branch
        sampleRepo.git("checkout", "-b", "feature2");
        sampleRepo.write("Jenkinsfile", "echo \"branch=${env.BRANCH_NAME}\"; "+"node {" +
            "   stage ('Build'); " +
            "   echo ('Building'); " +
            "   stage ('Test'); " +
            "   echo ('Testing'); " +
            "   stage ('Deploy'); " +
            "   echo ('Deploying'); " +
            "}");
        ScriptApproval.get().approveSignature("method java.lang.String toUpperCase");
        sampleRepo.write("file", "subsequent content2");
        sampleRepo.git("commit", "--all", "--message=tweaked2");
    }
}
