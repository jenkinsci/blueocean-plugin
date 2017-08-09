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

import hudson.Extension;
import hudson.model.Item;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitTool;
import io.jenkins.blueocean.commons.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.TimeZone;
import javax.annotation.Nonnull;
import jenkins.branch.MultiBranchProject;
import jenkins.plugins.git.GitSCMFileSystem;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMFileSystem;
import jenkins.scm.api.SCMHead;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;

/**
 * Uses the SCM Git cache to load/save content "as efficiently as possible"
 * @author kzantow
 */
@Extension
public class GitSCMReadSaveService extends GitReadSaveService {
    private static boolean useBareRepo = true;
    
    public static class GitSCMReadSaveRequest extends GitReadSaveRequest {
        final Item item;
        public GitSCMReadSaveRequest(Item item, String readUrl, String writeUrl, String writeCredentialId, String readCredentialId, String branch, String commitMessage, String sourceBranch, String filePath, byte[] contents, GitTool gitTool, GitSCMSource gitSource) throws IOException, InterruptedException {
            super(readUrl, writeUrl, writeCredentialId, readCredentialId, branch, commitMessage, sourceBranch, filePath, contents, gitTool, gitSource);
            this.item = item;
        }

        @Override
        byte[] read() throws IOException, InterruptedException {
            if (useBareRepo) return readFromBareRepo();
            GitSCMFileSystem fs = getFilesystem();
            return fs.invoke(new GitSCMFileSystem.FSFunction<byte[]>() {
                @Override
                public byte[] invoke(Repository repository) throws IOException, InterruptedException {
                    Git activeRepo = getActiveRepository(repository);
                    File repoDir = activeRepo.getRepository().getDirectory().getParentFile();
                    try {
                        File f = new File(repoDir, filePath);
                        if (f.canRead()) {
                            return IOUtils.toByteArray(new FileInputStream(f));
                        }
                        return null;
                    } finally {
                        FileUtils.deleteDirectory(repoDir);
                    }
                }
            });
        }
        
        private static final String REMOTE_ORIGIN_REF_BASE = "refs/remotes/origin/";
        byte[] readFromBareRepo() throws IOException, InterruptedException {
            GitSCMFileSystem fs = getFilesystem();
            return fs.invoke(new GitSCMFileSystem.FSFunction<byte[]>() {
                @Override
                public byte[] invoke(Repository repository) throws IOException, InterruptedException {
                    try (Git git = new Git(repository)) {
                        return GitUtils.readFile(repository, REMOTE_ORIGIN_REF_BASE + branch, filePath);
                    }
                }
            });
        }

        @Override
        void save() throws IOException, InterruptedException, GitException, URISyntaxException {
            if (useBareRepo) {
                saveFromBareRepo();
                return;
            }
            
            GitSCMFileSystem fs = getFilesystem();
            fs.invoke(new GitSCMFileSystem.FSFunction<Void>() {
                @Override
                public Void invoke(Repository repository) throws IOException, InterruptedException {
                    Git activeRepo = getActiveRepository(repository);
                    File repoDir = activeRepo.getRepository().getDirectory().getParentFile();
                    try {
//                        if (!sourceBranch.equals(branch)) {
//                            CheckoutCommand checkout = activeRepo.checkout();
//                            checkout.setName(sourceBranch);
//                            checkout.call();
//                        }
                    
                        File f = new File(repoDir, filePath);
                        if (!f.exists() || f.canWrite()) {
                            try (Writer w = new OutputStreamWriter(new FileOutputStream(f), "utf-8")) {
                                w.write(new String(contents, "utf-8"));
                            }

                            try {
                                AddCommand add = activeRepo.add();
                                add.addFilepattern(filePath);
                                add.call();

                                CommitCommand commit = activeRepo.commit();
                                commit.setMessage(commitMessage);
                                commit.call();
                                
                                PushCommand push = activeRepo.push();
                                push.setRemote(readUrl);
                                push.call();
                            } catch (GitAPIException ex) {
                                throw new ServiceException.UnexpectedErrorException(ex.getMessage(), ex);
                            }

                            return null;
                        }
                        throw new ServiceException.UnexpectedErrorException("Unable to write " + filePath);
//                    } catch (GitAPIException ex) {
//                        throw new ServiceException.UnexpectedErrorException(ex.getMessage(), ex);
                    } finally {
                        FileUtils.deleteDirectory(repoDir);
                    }
                }
            });
        }
        
        private void saveFromBareRepo() throws IOException, InterruptedException, GitException, URISyntaxException {
            GitSCMFileSystem fs = getFilesystem();
            fs.invoke(new GitSCMFileSystem.FSFunction<Void>() {
                @Override
                public Void invoke(Repository db) throws IOException, InterruptedException {
                    // Adapted from: https://gist.github.com/porcelli/3882505
                    GitUtils.commit(db, REMOTE_ORIGIN_REF_BASE + branch, filePath, contents, "Jenkins", "jenkins@jenkins.x", commitMessage, TimeZone.getDefault(), new Date());
                    // See:
                    // https://github.com/eclipse/jgit/blob/master/org.eclipse.jgit.test/tst/org/eclipse/jgit/api/PushCommandTest.java#L165
                    String remote = readUrl;//"origin";
                    String localBranchSpec = REMOTE_ORIGIN_REF_BASE + branch;
                    
                    // git push <repository> <refspec>
                    // git push git@github.com:jenkinsci/blueocean-plugin.git refs/heads/master

                    try (Git git = new Git(db)) {
                        String theRefSpec = "+" + localBranchSpec + ":refs/heads/" + branch;
                        RefSpec spec = new RefSpec(localBranchSpec + ":" + branch);
//                        Iterable<PushResult> resultIterable = git.push().setRemote(remote)
//                                        .setRefSpecs(spec).call();
                        Iterable<PushResult> resultIterable = git.push()
                            .add(theRefSpec)
                            .setRemote(remote)
                            .call();
                        PushResult result = resultIterable.iterator().next();
                        if (result.getRemoteUpdates().size() < 1) {
                            throw new RuntimeException("No remote updates");
                        }
                    } catch (GitAPIException ex) {
                        throw new ServiceException.UnexpectedErrorException("Unable to save and push Jenkinsfile: " + ex.getMessage(), ex);
                    }
                    return null;
                }
            });
        }
        
        private @Nonnull GitSCMFileSystem getFilesystem() throws IOException, InterruptedException {
            MultiBranchProject mbp = (MultiBranchProject)item;
            GitSCMSource source = (GitSCMSource)mbp.getSCMSources().iterator().next();
            GitSCMFileSystem fs = (GitSCMFileSystem)SCMFileSystem.of(source, new SCMHead(branch));
            if (fs == null) {
                throw new ServiceException.NotFoundException("No file found");
            }
            return fs;
        }
        
        private @Nonnull Git getActiveRepository(Repository repository) throws IOException {
            try {
                // Clone the bare repository
                File cloneDir = File.createTempFile("clone", "");
                
                if (cloneDir.exists()) {
                    if (cloneDir.isDirectory()) {
                        FileUtils.deleteDirectory(cloneDir);
                    } else {
                        if (!cloneDir.delete()) {
                            throw new ServiceException.UnexpectedErrorException("Unable to delete repository clone");
                        }
                    }
                }
                if (!cloneDir.mkdirs()) {
                    throw new ServiceException.UnexpectedErrorException("Unable to create repository clone directory");
                }
                
                Git gitClient = Git.cloneRepository()
                    .setCloneAllBranches(false)
                    .setProgressMonitor(new CloneProgressMonitor())
                    .setURI(repository.getDirectory().getCanonicalPath())
                    .setDirectory(cloneDir)
                    .call();

                RemoteRemoveCommand rrm = gitClient.remoteRemove();
                rrm.setName("origin");
                rrm.call();
                
                RemoteAddCommand radd = gitClient.remoteAdd();
                radd.setName("origin");
                radd.setUri(new URIish(readUrl));
                radd.call();
                
                FetchCommand fetch = gitClient.fetch();
                fetch.call();
                
                if (!StringUtils.isEmpty(sourceBranch) && !sourceBranch.equals(branch)) {
                    CheckoutCommand checkout = gitClient.checkout();
                    checkout.setStartPoint("origin/" + sourceBranch);
                    checkout.setName(sourceBranch);
                    checkout.setCreateBranch(true); // to create a new local branch
                    checkout.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK);
                    checkout.call();
                    
                    checkout = gitClient.checkout();
                    checkout.setName(branch);
                    checkout.setCreateBranch(true); // this *should* be a new branch
                    checkout.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK);
                    checkout.call();
                } else {
                    CheckoutCommand checkout = gitClient.checkout();
                    checkout.setStartPoint("origin/" + branch);
                    checkout.setName(branch);
                    checkout.setCreateBranch(true); // to create a new local branch
                    checkout.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK);
                    checkout.call();
                }
                
                return gitClient;
            } catch (GitAPIException | URISyntaxException ex) {
                throw new ServiceException.UnexpectedErrorException("Unable to get working repository directory", ex);
            }
        }
    }
    
    public static class CloneProgressMonitor implements ProgressMonitor {
        @Override
        public void beginTask(String string, int i) {
            System.out.println("beginTask" + string + " " + i);
        }

        @Override
        public void start(int i) {
            System.out.println("start " + i);
        }

        @Override
        public void update(int i) {
            System.out.println("update " + i);
        }

        @Override
        public void endTask() {
            System.out.println("endTask ");
        }

        @Override
        public boolean isCancelled() {
            System.out.println("isCancelled ");
            return false;
        }
    }
}
