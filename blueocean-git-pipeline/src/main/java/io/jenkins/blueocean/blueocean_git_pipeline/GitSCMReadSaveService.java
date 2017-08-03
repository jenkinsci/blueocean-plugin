/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.jenkins.blueocean.blueocean_git_pipeline;

import hudson.Extension;
import hudson.model.Item;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitTool;
import io.jenkins.blueocean.commons.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
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
import org.eclipse.jgit.transport.URIish;

/**
 *
 * @author kzantow
 */
@Extension
public class GitSCMReadSaveService extends GitReadSaveService {
    public static class GitSCMReadSaveRequest extends GitReadSaveRequest {
        final Item item;
        public GitSCMReadSaveRequest(Item item, String readUrl, String writeUrl, String writeCredentialId, String readCredentialId, String branch, String commitMessage, String sourceBranch, String filePath, byte[] contents, GitTool gitTool, GitSCMSource gitSource) throws IOException, InterruptedException {
            super(readUrl, writeUrl, writeCredentialId, readCredentialId, branch, commitMessage, sourceBranch, filePath, contents, gitTool, gitSource);
            this.item = item;
        }

        @Override
        byte[] read() throws IOException, InterruptedException {
            GitSCMFileSystem fs = getFilesystem();
            return fs.invoke(new GitSCMFileSystem.FSFunction<byte[]>() {
                @Override
                public byte[] invoke(Repository repository) throws IOException, InterruptedException {
                    Git activeRepo = getActiveRepository(repository);
                    File repoDir = activeRepo.getRepository().getDirectory().getParentFile();
                    System.out.println("Repo cloned to: " + repoDir.getCanonicalPath());
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

        @Override
        void save() throws IOException, InterruptedException, GitException, URISyntaxException {
            GitSCMFileSystem fs = getFilesystem();
            fs.invoke(new GitSCMFileSystem.FSFunction<Void>() {
                @Override
                public Void invoke(Repository repository) throws IOException, InterruptedException {
                    Git activeRepo = getActiveRepository(repository);
                    File repoDir = activeRepo.getRepository().getDirectory().getParentFile();
                    System.out.println("Repo cloned to: " + repoDir.getCanonicalPath());
                    try {
//                        if (!sourceBranch.equals(branch)) {
//                            CheckoutCommand checkout = activeRepo.checkout();
//                            checkout.setName(sourceBranch);
//                            checkout.call();
//                        }
                    
                        File f = new File(repoDir, filePath);
                        if (!f.exists() || f.canWrite()) {
                            try (FileWriter fw = new FileWriter(f)) {
                                fw.write(new String(contents, "utf-8"));
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
                        cloneDir.delete();
                    }
                }
                cloneDir.mkdirs();
                
                Git gitClient = Git.cloneRepository()
                    .setCloneAllBranches(false)
                    .setProgressMonitor(new ReadSaveProgressMonitor())
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
                ex.printStackTrace();
                throw new ServiceException.UnexpectedErrorException("Unable to get working repository directory", ex);
            }
        }
    }
    
    public static class ReadSaveProgressMonitor implements ProgressMonitor {
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
