package io.jenkins.blueocean.blueocean_git_pipeline;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.EnvVars;
import hudson.model.ItemGroup;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import hudson.security.ACL;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import java.io.ByteArrayInputStream;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

/**
 * @author Vivek Pandey
 */
class GitUtils {
    private static final Logger logger = LoggerFactory.getLogger(GitUtils.class);

    /**
     *  Calls 'git ls-remote -h uri' to check if git uri or supplied credentials are valid
     *
     * @param uri git repo uri
     * @param credentials credential to use when accessing git
     * @return list of Errors. Empty list means success.
     */
    static List<ErrorMessage.Error> validateCredentials(@Nonnull String uri, @Nullable StandardCredentials credentials) throws GitException{
        List<ErrorMessage.Error> errors = new ArrayList<>();
        Git git = new Git(TaskListener.NULL, new EnvVars());
        try {
            GitClient gitClient = git.getClient();
            if(credentials != null) {
                gitClient.addCredentials(uri, credentials);
            }
            gitClient.getRemoteReferences(uri,null, true,false);
        } catch (IOException | InterruptedException e) {
            logger.error("Error running git remote-ls: " + e.getMessage(), e);
            throw  new ServiceException.UnexpectedErrorException("Failed to create pipeline due to unexpected error: "+e.getMessage(), e);
        } catch (IllegalStateException | GitException e){
            logger.error("Error running git remote-ls: " + e.getMessage(), e);
            if(credentials != null) {
                // XXX: check for 'not authorized' is hack. Git plugin API (org.eclipse.jgit.transport.TransportHttp.connect())does not send
                //      back any error code so that we can distinguish between unauthorized vs bad url or some other type of errors.
                //      Where org.eclipse.jgit.transport.SshTransport.connect() throws IllegalStateException in case of unauthorized,
                //      org.eclipse.jgit.transport.HttpTransport.connect() throws TransportException with error code 'not authorized'
                //      appended to the message.
                if(e instanceof IllegalStateException || e.getMessage().endsWith("not authorized")){
                    errors.add(new ErrorMessage.Error("scmConfig.credentialId",
                                    ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                                    "Invalid credentialId: " + credentials.getId()));
                }
            }else if (e.getMessage().contains("Authentication is required") || e.getMessage().contains("connection is not authenticated")) {
                errors.add(new ErrorMessage.Error("scmConfig.credentialId", ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                        e.getMessage()));
            }else{
                errors.add(new ErrorMessage.Error("scmConfig.uri", ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                        e.getMessage()));
            }
        }
        return errors;
    }

    static StandardCredentials getCredentials(ItemGroup owner, String uri, String credentialId){
        StandardCredentials standardCredentials =  CredentialsUtils.findCredential(credentialId, StandardCredentials.class, new BlueOceanDomainRequirement());
        if(standardCredentials == null){
            standardCredentials = CredentialsMatchers
                    .firstOrNull(
                            CredentialsProvider.lookupCredentials(StandardCredentials.class, owner,
                                    ACL.SYSTEM, URIRequirementBuilder.fromUri(uri).build()),
                            CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialId),
                                    GitClient.CREDENTIALS_MATCHER));
        }

        return standardCredentials;
    }

    public static void commit(final Repository repo, final String refName, final String path, final byte[] contents,
            final String name, final String email, final String message, final TimeZone timeZone, final Date when) {

        final String gitPath = path; // fixPath(path); ???

        final PersonIdent author = buildPersonIdent(repo, name, email, timeZone, when);

        try {
            final ObjectInserter odi = repo.newObjectInserter();
            try {
                // Create the in-memory index of the new/updated issue.
                final ObjectId headId = repo.resolve(refName + "^{commit}");
                final DirCache index = createTemporaryIndex(repo, headId, gitPath, contents);
                final ObjectId indexTreeId = index.writeTree(odi);

                // Create a commit object
                final CommitBuilder commit = new CommitBuilder();
                commit.setAuthor(author);
                commit.setCommitter(author);
                commit.setEncoding(Constants.CHARACTER_ENCODING);
                commit.setMessage(message);
                //headId can be null if the repository has no commit yet
                if (headId != null) {
                    commit.setParentId(headId);
                }
                commit.setTreeId(indexTreeId);

                // Insert the commit into the repository
                final ObjectId commitId = odi.insert(commit);
                odi.flush();

                final RevWalk revWalk = new RevWalk(repo);
                try {
                    final RevCommit revCommit = revWalk.parseCommit(commitId);
                    final RefUpdate ru = repo.updateRef(refName);
                    if (headId == null) {
                        ru.setExpectedOldObjectId(ObjectId.zeroId());
                    } else {
                        ru.setExpectedOldObjectId(headId);
                    }
                    ru.setNewObjectId(commitId);
                    ru.setRefLogMessage("commit: " + revCommit.getShortMessage(), false);
                    final RefUpdate.Result rc = ru.forceUpdate();
                    switch (rc) {
                        case NEW:
                        case FORCED:
                        case FAST_FORWARD:
                            break;
                        case REJECTED:
                        case LOCK_FAILURE:
                            throw new ConcurrentRefUpdateException(JGitText.get().couldNotLockHEAD, ru.getRef(), rc);
                        default:
                            throw new JGitInternalException(MessageFormat.format(JGitText.get().updatingRefFailed, Constants.HEAD, commitId.toString(), rc));
                    }

                } finally {
                    //revWalk.release();
                }
            } finally {
                //odi.release();
                repo.close();
            }
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static PersonIdent buildPersonIdent(final Repository repo, final String name, final String email,
            final TimeZone timeZone, final Date when) {
        final TimeZone tz = timeZone == null ? TimeZone.getDefault() : timeZone;

        if (name != null) {
            if (when != null) {
                return new PersonIdent(name, email, when, tz);
            } else {
                return new PersonIdent(name, email);
            }
        }
        return new PersonIdent(repo);
    }

    /**
     * Creates an in-memory index of the issue change.
     */
    private static DirCache createTemporaryIndex(final Repository repo, final ObjectId headId, final String path, final byte[] contents) {

        final DirCache inCoreIndex = DirCache.newInCore();
        final DirCacheBuilder dcBuilder = inCoreIndex.builder();
        final ObjectInserter inserter = repo.newObjectInserter();
        
        long lastModified = System.currentTimeMillis();

        try {
            if (contents != null) {
                final DirCacheEntry dcEntry = new DirCacheEntry(path);
                dcEntry.setLength(contents.length);
                dcEntry.setLastModified(lastModified);
                dcEntry.setFileMode(FileMode.REGULAR_FILE);

                final InputStream inputStream = new ByteArrayInputStream(contents);
                try {
                    dcEntry.setObjectId(inserter.insert(Constants.OBJ_BLOB, contents.length, inputStream));
                } finally {
                    inputStream.close();
                }

                dcBuilder.add(dcEntry);
            }

            if (headId != null) {
                final TreeWalk treeWalk = new TreeWalk(repo);
                final int hIdx = treeWalk.addTree(new RevWalk(repo).parseTree(headId));
                treeWalk.setRecursive(true);

                while (treeWalk.next()) {
                    final String walkPath = treeWalk.getPathString();
                    final CanonicalTreeParser hTree = treeWalk.getTree(hIdx, CanonicalTreeParser.class);

                    if (!walkPath.equals(path)) {
                        // add entries from HEAD for all other paths
                        // create a new DirCacheEntry with data retrieved from HEAD
                        final DirCacheEntry dcEntry = new DirCacheEntry(walkPath);
                        dcEntry.setObjectId(hTree.getEntryObjectId());
                        dcEntry.setFileMode(hTree.getEntryFileMode());

                        // add to temporary in-core index
                        dcBuilder.add(dcEntry);
                    }
                }
                //treeWalk.release();
            }

            dcBuilder.finish();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            //inserter.release();
        }

        if (contents == null) {
            final DirCacheEditor editor = inCoreIndex.editor();
            editor.add(new DirCacheEditor.DeleteTree(path));
            editor.finish();
        }

        return inCoreIndex;
    }

    static byte[] readFile(Repository repository, String ref, String filePath) {
        try (ObjectReader reader = repository.newObjectReader()) {
            ObjectId branchRef = repository.resolve(ref); // repository.exactRef(ref);
            RevWalk revWalk = new RevWalk(repository);
            RevCommit commit = revWalk.parseCommit(branchRef);
            // and using commit's tree find the path
            RevTree tree = commit.getTree();
            TreeWalk treewalk = TreeWalk.forPath(reader, filePath, tree);
            if (treewalk != null) {
                // use the blob id to read the file's data
                return reader.open(treewalk.getObjectId(0)).getBytes();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }
}
