/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
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
package io.jenkins.blueocean.blueocean_git_pipeline;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.google.common.collect.ImmutableMap;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import hudson.model.User;
import hudson.remoting.Base64;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.impl.pipeline.PipelineBaseTest;
import io.jenkins.blueocean.service.embedded.util.SSHKeyUtils;
import io.jenkins.blueocean.service.embedded.util.UserSSHKeyManager;
import jenkins.plugins.git.GitSampleRepoRule;
import org.apache.commons.io.FileUtils;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.nativefs.NativeFileSystemFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.random.JceRandomFactory;
import org.apache.sshd.common.random.SingletonRandomFactory;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.OsUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuth;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.auth.pubkey.UserAuthPublicKeyFactory;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.*;
import org.jenkinsci.plugins.gitserver.ssh.SshCommandFactoryImpl;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.junit.*;
import org.junit.runners.Parameterized;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Testing the git load/save backend
 * @author kzantow
 */
public class GitReadSaveTest extends PipelineBaseTest {
    @Rule
    public GitSampleRepoRule repoWithJenkinsfiles = new GitSampleRepoRule();

    @Rule
    public GitSampleRepoRule repoNoJenkinsfile = new GitSampleRepoRule();

    @Rule
    public GitSampleRepoRule repoForSSH = new GitSampleRepoRule();

    private SshServer sshd;

    @Parameterized.Parameters
    public static Object[] data() {
        return new Object[] { null, "TestOrg" };
    }

    public GitReadSaveTest() {
        this("jenkins");
    }

    private GitReadSaveTest(String blueOrganisation) {
        System.out.println("setting org root to: " + blueOrganisation);
        GitScmTest.TestOrganizationFactoryImpl.orgRoot = blueOrganisation;
    }

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        setupScm();
    }

    private String getOrgName() {
        return OrganizationFactory.getInstance().list().iterator().next().getName();
    }

    private static final String masterPipelineScript = "pipeline { stage('Build 1') { steps { echo 'build' } } }";
    private static final String branchPipelineScript = "pipeline { stage('Build 2') { steps { echo 'build' } } }";
    private static final String newPipelineScript = "pipeline { stage('Build 3') { steps { echo 'build' } } }";

    static class GitCommandFactory extends SshCommandFactoryImpl implements CommandFactory {
        final File cwd;

        GitCommandFactory(File cwd) {
            this.cwd = cwd;
        }

        @Override
        public Command createCommand(String command) {
            System.out.println("Incoming command: " + command);
            CommandLine cmd = new CommandLine(command);
            try {
                Process proc = new ProcessBuilder(cmd.toArray(new String[cmd.size()]))
                    .directory(cwd)
                    .redirectErrorStream(true)
                    .inheritIO()
                    .start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new ProcessShellFactory(cmd.toArray(new String[cmd.size()])).create();
        }
    }

    private void setupScm() throws Exception {
        // create git repo
        repoWithJenkinsfiles.init();
        repoWithJenkinsfiles.write("Jenkinsfile", masterPipelineScript);
        repoWithJenkinsfiles.write("file", "initial content");
        repoWithJenkinsfiles.git("add", "Jenkinsfile");
        repoWithJenkinsfiles.git("commit", "--all", "--message=flow");

        //create feature branch
        repoWithJenkinsfiles.git("checkout", "-b", "feature/ux-1");
        repoWithJenkinsfiles.write("Jenkinsfile", branchPipelineScript);
        ScriptApproval.get().approveSignature("method java.lang.String toUpperCase");
        repoWithJenkinsfiles.write("file", "subsequent content1");
        repoWithJenkinsfiles.git("commit", "--all", "--message=tweaked1");

        // we're using this to test push/pull, allow pushes to current branch, we reset it to match
        repoWithJenkinsfiles.git("config", "--local", "--add", "receive.denyCurrentBranch", "false");

        repoNoJenkinsfile.init();
        repoNoJenkinsfile.write("file", "nearly empty file");
        repoNoJenkinsfile.git("add", "file");
        repoNoJenkinsfile.git("commit", "--all", "--message=initilaize the repo");

        // we're using this to test push/pull, allow pushes to current branch, we reset it to match
        repoNoJenkinsfile.git("config", "--local", "--add", "receive.denyCurrentBranch", "false");
    }

    private void startSSH() throws Exception {
        repoForSSH.init();
        repoForSSH.write("Jenkinsfile", masterPipelineScript);
        repoForSSH.git("add", "Jenkinsfile");
        repoForSSH.git("commit", "--all", "--message=initilaize the repo");

        // we're using this to test push/pull, allow pushes to current branch, we reset it to match
        repoForSSH.git("config", "--local", "--add", "receive.denyCurrentBranch", "false");

        final File rootFsDir = repoForSSH.getRoot().getParentFile();

        // Set up an SSH server with access to a git repo
        User user = login();
        final String userName = user.getId();
        final BasicSSHUserPrivateKey key = UserSSHKeyManager.getOrCreate(user);
        final JSch jsch = new JSch();
        final KeyPair pair = KeyPair.load(jsch, key.getPrivateKey().getBytes(), null);

        // Set up sshd defaults, bind go IPv4 and random non-privileged port
        sshd = SshServer.setUpDefaultServer();
        sshd.setHost("0.0.0.0");
        sshd.setPort(Integer.parseInt(System.getProperty("TEST_SSH_SERVER_PORT", "0")));

        // Set up an RSA host key
        File keyFile = new File(System.getProperty("TEST_SSH_SERVER_KEY_FILE", File.createTempFile("hostkey", "ser").getCanonicalPath()));
        AbstractGeneratorHostKeyProvider hostKeyProvider =
            new SimpleGeneratorHostKeyProvider(keyFile);
        hostKeyProvider.setAlgorithm("RSA");
        sshd.setKeyPairProvider(hostKeyProvider);

        // Set key exchange factories so recent clients can connect
//        sshd.setKeyExchangeFactories(Arrays.<NamedFactory<KeyExchange>>asList(
//            BuiltinDHFactories.dhg14, // this is only registered by default with BC provider... JCE doesn't support 2048 bit?
//            BuiltinDHFactories.dhg1));
        sshd.setRandomFactory(new SingletonRandomFactory(new JceRandomFactory()));

        // Set up a default shell
        if (OsUtils.isUNIX()) {
            ProcessShellFactory shellFactory = new ProcessShellFactory(new String[]{"/bin/sh", "-i", "-l"}) {
                private TtyFilterOutputStream in;
                private TtyFilterInputStream out;
                private TtyFilterInputStream err;
                @Override
                protected InvertedShell createInvertedShell() {
                    return new ProcessShell() {
                        public void start(Environment env) throws IOException {
                            Map<String, String> varsMap = this.resolveShellEnvironment(env.getEnv());
                            List<String> command = getCommand();
                            for(int i = 0; i < command.size(); ++i) {
                                String cmd = (String)command.get(i);
                                if ("$USER".equals(cmd)) {
                                    cmd = (String)varsMap.get("USER");
                                    command.set(i, cmd);
                                    //cmdValue = GenericUtils.join(command, ' ');
                                }
                            }

                            ProcessBuilder builder = new ProcessBuilder(command);
                            Map modes;
                            if (GenericUtils.size(varsMap) > 0) {
                                try {
                                    modes = builder.environment();
                                    modes.putAll(varsMap);
                                } catch (Exception var5) {
                                    throw new RuntimeException(var5);

                                }
                            }

                            if (this.log.isDebugEnabled()) {
                                this.log.debug("Starting shell with command: '{}' and env: {}", builder.command(), builder.environment());
                            }

                            Process process = builder.start();
                            modes = this.resolveShellTtyOptions(env.getPtyModes());
                            out = new TtyFilterInputStream(process.getInputStream(), modes);
                            err = new TtyFilterInputStream(process.getErrorStream(), modes);
                            in = new TtyFilterOutputStream(process.getOutputStream(), err, modes);
                        }
                    };
                }
            };
            sshd.setShellFactory(shellFactory);//,
//                EnumSet.of(ProcessShellFactory.TtyOptions.ONlCr)));
        } else {
            ProcessShellFactory shellFactory = new ProcessShellFactory(new String[]{"cmd.exe "});
            sshd.setShellFactory(shellFactory);//,
//                EnumSet.of(ProcessShellFactory.TtyOptions.Echo, ProcessShellFactory.TtyOptions.ICrNl, ProcessShellFactory.TtyOptions.ONlCr)));
        }

        InteractiveProcessShellFactory shellFactory = new InteractiveProcessShellFactory() {
            @Override
            public Command create() {
                return super.create();
            }
        };
        //sshd.setShellFactory(shellFactory);

        FileSystemFactory fileSystemFactory = new VirtualFileSystemFactory(repoForSSH.getRoot().toPath()) ;
        sshd.setFileSystemFactory(fileSystemFactory);

        // Set up git + scp command support
        //CommandFactory gitCommandFactory = new GitCommandFactory(cwd);
        //sshd.setCommandFactory(new ScpCommandFactory(gitCommandFactory));

        // Set up the user's SSH key for authentication
        PublickeyAuthenticator authenticator = new PublickeyAuthenticator() {
            @Override
            public boolean authenticate(String username, PublicKey key, ServerSession session) {
                File localPublicKey = new File(System.getProperty("user.home") + "/.ssh/id_rsa.pub");
                try {
                    byte[] incoming = SSHKeyUtils.encodePublicKey((RSAPublicKey)key);
                    String incomingHex = Base64.encode(incoming);
                    if (localPublicKey.canRead() && FileUtils.readFileToString(localPublicKey).contains(incomingHex)) {
                        return true;
                    }
                    String userPublicKey = Base64.encode(pair.getPublicKeyBlob());
                    System.out.println(" ---- Authentication request for: " + username + " with key: " + incomingHex + " user's public key is: " + userPublicKey);
                    return userName.equals(username) && userPublicKey.equals(incomingHex);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        sshd.setPublickeyAuthenticator(authenticator);
        sshd.setUserAuthFactories(Collections.<NamedFactory<UserAuth>>singletonList(new UserAuthPublicKeyFactory()));

        // Try to serve up filesystem stuff
//        NativeFileSystemFactory fsFactory = new NativeFileSystemFactory() {
//            @Override
//            public FileSystemView createFileSystemView(Session session) {
//                return new FileSystemView() {
//                    @Override
//                    public SshFile getFile(String file) {
//                        System.out.println("Get file: " + file);
//                        if ("repo".equals(file)) {
//                            return new RepoSshFile("/", rootFsDir, userName);
//                        }
//                        if ("repo.git".equals(file)) {
//                            return new RepoSshFile("/", new File(rootFsDir, ".git"), userName);
//                        }
//                        return null;
//                    }
//                    @Override
//                    public SshFile getFile(SshFile baseDir, String file) {
//                        System.out.println("Get dir: " + baseDir.getAbsolutePath() + " file: " + file);
//                        if ("repo.git".equals(file)) {
//                            return new RepoSshFile("/", new File(rootFsDir, ".git"), userName);
//                        }
//                        List<SshFile> subdirs = new ArrayList<>();
//                        // Get the .git dir
//                        SshFile repoDir = baseDir;
//                        while (repoDir != null && !"repo.git".equals(repoDir.getName())) {
//                            subdirs.add(0, repoDir);
//                            repoDir = repoDir.getParentFile();
//                        }
//
//                        File f = new File(rootFsDir, ".git");
//                        if (repoDir != null) {
//                            for (SshFile sf : subdirs) {
//                                f = new File(f, sf.getName());
//                            }
//                            return new RepoSshFile(file, new File(f, file), userName);
//                        }
//                        return null;
//                    }
//                };
//            }
//        };
//        sshd.setFileSystemFactory(fsFactory);

        // Go, go, go
        sshd.start();
    }
//
//    class RepoSshFile extends NativeSshFile {
//        RepoSshFile(final String fileName, final File file, final String userName) {
//            super(fileName, file, userName);
//        }
//    }

    @After
    public void stopSSHServer() throws InterruptedException, IOException {
        if (sshd != null) {
            sshd.stop(true);
        }
    }

    @Test
    public void testGitCloneReadWrite() throws Exception {
        testGitReadWrite(GitReadSaveService.ReadSaveType.CLONE, repoWithJenkinsfiles.getRoot(), masterPipelineScript);
        testGitReadWrite(GitReadSaveService.ReadSaveType.CLONE, repoNoJenkinsfile.getRoot(), null);
    }

    @Test
    public void testGitCacheCloneReadWrite() throws Exception {
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_CLONE, repoWithJenkinsfiles.getRoot(), masterPipelineScript);
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_CLONE, repoNoJenkinsfile.getRoot(), null);
    }

    @Test
    public void testBareRepoReadWrite() throws Exception {
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_BARE, repoWithJenkinsfiles.getRoot(), masterPipelineScript);
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_BARE, repoNoJenkinsfile.getRoot(), null);
    }

    //@Test
    public void bareRepoReadWriteOverSSH() throws Exception {
        startSSH();

        String ssh = "ssh -p " + sshd.getPort() + " bob@127.0.0.1";
        System.out.println(ssh);
        String userHostPort = "bob@127.0.0.1:" + sshd.getPort();
        String remote = "ssh://" + userHostPort + "/repo/repo.git";
        //remote = userHostPort + ":repo/repo.git";
        testGitReadWrite(GitReadSaveService.ReadSaveType.CACHE_BARE, remote, repoForSSH.getRoot(), masterPipelineScript);
    }

    private void testGitReadWrite(final @Nonnull GitReadSaveService.ReadSaveType type, @Nonnull File remoteDir, @Nullable String startPipelineScript) throws Exception {
        testGitReadWrite(type, remoteDir.getCanonicalPath(), remoteDir, startPipelineScript);
    }

    private void testGitReadWrite(final @Nonnull GitReadSaveService.ReadSaveType type, @Nonnull String remote, @Nonnull File remoteDir, @Nullable String startPipelineScript) throws Exception {
        GitReadSaveService.setType(type);

        String jobName = remoteDir.getName();

        User user = login();

        Map r = new RequestBuilder(baseUrl)
                .status(201)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .post("/organizations/" + getOrgName() + "/pipelines/")
                .data(ImmutableMap.of(
                        "name", jobName,
                        "$class", "io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest",
                        "scmConfig", ImmutableMap.of(
                            "uri", remote,
                            "credentialId", UserSSHKeyManager.getOrCreate(user).getId())
                )).build(Map.class);

        assertEquals(jobName, r.get("name"));

        String urlJobPrefix = "/organizations/" + getOrgName() + "/pipelines/" + jobName;

        r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get(urlJobPrefix + "/scm/content/?branch=master&path=Jenkinsfile&type="+type.name())
                .build(Map.class);

        String base64Data = (String)((Map)r.get("content")).get("base64Data");

        assertEquals(startPipelineScript, base64Data == null ? null : new String(Base64.decode(base64Data), "utf-8"));

        // Update the remote
        String newBase64Data = Base64.encode(newPipelineScript.getBytes("utf-8"));
        Map<String,String> content = new HashMap<>();
        content.put("message", "Save Jenkinsfile");
        content.put("path", "Jenkinsfile");
        content.put("branch", "master");
        content.put("sourceBranch", "master");
        content.put("repo", jobName); // if no repo, this is not in an org folder
        content.put("sha", "");
        content.put("base64Data", newBase64Data);
        content.put("type", type.name());

        new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .put(urlJobPrefix + "/scm/content/")
                .data(ImmutableMap.of("content", content))
                .build(Map.class);

        // Check to make sure the remote was actually updated:
        // refs udpated in our sample repo, not working tree, update it to get contents:
        repoWithJenkinsfiles.git("reset", "--hard", "refs/heads/master");
        String remoteJenkinsfile = FileUtils.readFileToString(new File(repoWithJenkinsfiles.getRoot(), "Jenkinsfile"));
        Assert.assertEquals(newPipelineScript, remoteJenkinsfile);

        // check to make sure we get the same thing from the service
        r = new RequestBuilder(baseUrl)
                .status(200)
                .jwtToken(getJwtToken(j.jenkins, user.getId(), user.getId()))
                .get(urlJobPrefix + "/scm/content/?branch=master&path=Jenkinsfile&type="+type.name())
                .build(Map.class);

        base64Data = (String)((Map)r.get("content")).get("base64Data");
        Assert.assertEquals(base64Data, newBase64Data);
    }
}
