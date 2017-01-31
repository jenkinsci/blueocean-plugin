package io.jenkins.blueocean.blueocean_git_pipeline;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.collect.ImmutableMap;
import hudson.ExtensionList;
import hudson.model.Item;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.rest.model.scm.GitSampleRepoRule;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

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
    public void shouldGetForbiddenForBadCredentialIdOnCreate1() throws IOException {

        SystemCredentialsProvider.ProviderImpl system = ExtensionList.lookup(CredentialsProvider.class).get(SystemCredentialsProvider.ProviderImpl.class);
        CredentialsStore systemStore = system.getStore(j.getInstance());
        systemStore.addDomain(new Domain("domain1", null, null));

        Map<String, Object> resp = post("/organizations/jenkins/credentials/system/domains/domain1/credentials/",
                ImmutableMap.of("credentials",
                        new ImmutableMap.Builder<String,Object>()
                                .put("privateKeySource", ImmutableMap.of(
                                        "privateKey", "abcabc1212",
                                        "stapler-class", "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey$DirectEntryPrivateKeySource"))
                                .put("passphrase", "ssh2")
                                .put("scope", "GLOBAL")
                                .put("description", "ssh2 desc")
                                .put("$class", "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey")
                                .put("username", "ssh2").build()
                )
                , 201);

        String credentialId = (String) resp.get("id");
        Assert.assertNotNull(credentialId);

        post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", "git@github.com:vivek/capability-annotation.git",
                                "credentialId", credentialId)
                ), 403);

    }


    @Test
    public void shouldGetForbiddenForBadCredentialIdOnCreate2() throws IOException {

        SystemCredentialsProvider.ProviderImpl system = ExtensionList.lookup(CredentialsProvider.class).get(SystemCredentialsProvider.ProviderImpl.class);
        CredentialsStore systemStore = system.getStore(j.getInstance());
        systemStore.addDomain(new Domain("domain1", null, null));

        Map<String, Object> resp = post("/organizations/jenkins/credentials/system/domains/domain1/credentials/",
                ImmutableMap.of("credentials",
                        new ImmutableMap.Builder<String,Object>()
                                .put("password", "abcd")
                                .put("stapler-class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                                .put("scope", "GLOBAL")
                                .put("description", "joe desc")
                                .put("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                                .put("username", "joe").build()
                )
                , 201);

        String credentialId = (String) resp.get("id");
        Assert.assertNotNull(credentialId);

        post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", "git@github.com:vivek/capability-annotation.git",
                                "credentialId", credentialId)
                ), 403);

    }

    @Test
    public void shouldGetBadRequestForBadGitUriOnCreate() throws IOException {

        post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", "/sdsd/sdsd/sdsd")
                ), 400);

    }


    @Test
    public void shouldGetForbiddenForBadCredentialIdOnUpdate1() throws IOException {

        String mbp = createMbp();


        SystemCredentialsProvider.ProviderImpl system = ExtensionList.lookup(CredentialsProvider.class).get(SystemCredentialsProvider.ProviderImpl.class);
        CredentialsStore systemStore = system.getStore(j.getInstance());
        systemStore.addDomain(new Domain("domain1", null, null));

        Map<String,Object> resp = post("/organizations/jenkins/credentials/system/domains/domain1/credentials/",
                ImmutableMap.of("credentials",
                        new ImmutableMap.Builder<String,Object>()
                                .put("privateKeySource", ImmutableMap.of(
                                        "privateKey", "abcabc1212",
                                        "stapler-class", "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey$DirectEntryPrivateKeySource"))
                                .put("passphrase", "ssh2")
                                .put("scope", "GLOBAL")
                                .put("description", "ssh2 desc")
                                .put("$class", "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey")
                                .put("username", "ssh2").build()
                )
                , 201);

        String credentialId = (String) resp.get("id");
        Assert.assertNotNull(credentialId);

        put("/organizations/jenkins/pipelines/"+mbp+"/",
                ImmutableMap.of("name", mbp,
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineUpdateRequest",
                        "scmConfig", ImmutableMap.of("uri", "git@github.com:vivek/capability-annotation.git",
                                "credentialId", credentialId)
                ), 403);

    }


    @Test
    public void shouldGetForbiddenForBadCredentialIdOnUpdate2() throws IOException {

        String mbp = createMbp();
        SystemCredentialsProvider.ProviderImpl system = ExtensionList.lookup(CredentialsProvider.class).get(SystemCredentialsProvider.ProviderImpl.class);
        CredentialsStore systemStore = system.getStore(j.getInstance());
        systemStore.addDomain(new Domain("domain1", null, null));

        Map<String, Object> resp = post("/organizations/jenkins/credentials/system/domains/domain1/credentials/",
                ImmutableMap.of("credentials",
                        new ImmutableMap.Builder<String,Object>()
                                .put("password", "abcd")
                                .put("stapler-class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                                .put("scope", "GLOBAL")
                                .put("description", "joe desc")
                                .put("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl")
                                .put("username", "joe").build()
                )
                , 201);

        String credentialId = (String) resp.get("id");
        Assert.assertNotNull(credentialId);

        put("/organizations/jenkins/pipelines/"+mbp+"/",
                ImmutableMap.of("name", mbp,
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineUpdateRequest",
                        "scmConfig", ImmutableMap.of("uri", "git@github.com:vivek/capability-annotation.git",
                                "credentialId", credentialId)
                ), 403);

    }

    @Test
    public void shouldGetBadRequestForBadGitUriOnUpdate() throws IOException {

        String mbp = createMbp();
        put("/organizations/jenkins/pipelines/"+mbp+"/",
                ImmutableMap.of("name", mbp,
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineUpdateRequest",
                        "scmConfig", ImmutableMap.of("uri", "/sdsd/sdsd/sdsd")
                ), 400);

    }


    @Test
    public void shouldCreateGitMbp(){

        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
            ImmutableMap.of("name", "demo",
                    "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                    "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl())
                ), 201);


        assertEquals("demo", resp.get("name"));
    }

    @Test
    public void shouldFailOnValidation1(){
        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of(
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl())
                ), 400);

        assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        assertEquals(errors.get(0).get("field"), "name");
        assertEquals(errors.get(0).get("code"), "MISSING");
        assertEquals(errors.get(1).get("field"), "$class");
        assertEquals(errors.get(1).get("code"), "MISSING");
    }

    @Test
    public void shouldFailOnValidation2(){
        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest"
                ), 400);

        assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        assertEquals(errors.get(0).get("field"), "scmConfig");
        assertEquals(errors.get(0).get("code"), "MISSING");
        assertNull(Jenkins.getInstance().getItem("demo"));
    }

    @Test
    public void shouldFailOnValidation3(){
        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of()), 400);

        assertEquals(resp.get("code"), 400);

        List<Map> errors = (List<Map>) resp.get("errors");

        assertEquals(errors.get(0).get("field"), "scmConfig.uri");
        assertEquals(errors.get(0).get("code"), "MISSING");
        assertNull(Jenkins.getInstance().getItem("demo"));

    }


    @Test
    public void shouldFailOnValidation4(){

        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl())
                ), 201);


        assertEquals("demo", resp.get("name"));

        resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl(), "credentialId", "sdsdsd")
                ), 400);
        List<Map<String,String>> errors = (List<Map<String,String>>) resp.get("errors");

        boolean nameFound = false;
        boolean credentialIdFound = false;
        for(Map<String,String> error:errors){
            if(error.get("field").equals("name")){
                nameFound = true;
                assertEquals(error.get("code"), "ALREADY_EXISTS");
            }else if(error.get("field").equals("scmConfig.credentialId")){
                credentialIdFound = true;
                assertEquals(error.get("code"), "NOT_FOUND");
            }
        }
        assertTrue(nameFound);
        assertTrue(credentialIdFound);
    }

    @Test
    public void shouldFailOnValidation5(){

        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl(), "credentialId", "sdsdsd")
                ), 400);
        List<Map<String,String>> errors = (List<Map<String,String>>) resp.get("errors");

        assertEquals("scmConfig.credentialId", errors.get(0).get("field"));
        assertEquals("NOT_FOUND", errors.get(0).get("code"));
        assertNull(Jenkins.getInstance().getItem("demo"));
    }

    private String createMbp(){
        Map<String,Object> resp = post("/organizations/jenkins/pipelines/",
                ImmutableMap.of("name", "demo",
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of("uri", sampleRepo.fileUrl())
                ), 201);


        assertEquals("demo", resp.get("name"));
        Item item = Jenkins.getInstance().getItem("demo");
        assertNotNull(item);
        assertTrue(item instanceof MultiBranchProject);
        return "demo";
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
