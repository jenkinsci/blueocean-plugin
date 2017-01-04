package io.jenkins.blueocean.blueocean_git_pipeline;

import com.google.common.collect.ImmutableMap;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.model.scm.GitSampleRepoRule;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class GitScmTest extends PipelineBaseTest {
    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Before
    public void setup() throws Exception{
        super.setup();
        setupScm();
    }

    @Test
    public void simpleOrgTest(){

        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
            ImmutableMap.of("name", "demo",
                    "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                    "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl())
                ), 201);


        Assert.assertEquals("demo", resp.get("name"));
    }

    private void setupScm() throws Exception {
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
    }
}
