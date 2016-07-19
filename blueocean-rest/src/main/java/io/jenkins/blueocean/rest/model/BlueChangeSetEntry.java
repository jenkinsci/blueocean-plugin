package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;


/**
 * Represents a single commit as a REST resource.
 *
 * @author Ivan Meredith
 */
public abstract class BlueChangeSetEntry extends Resource {
    /**
     * Returns a human readable display name of the commit number, revision number, and such thing
     * that identifies this entry.
     *
     *
     * @return
     *      null if such a concept doesn't make sense for the implementation. For example,
     *      in CVS there's no single identifier for commits. Each file gets a different revision number.
     */
    @Nullable
    @Exported
    public abstract  String getCommitId();

    /**
     * The user who made this change.
     *
     * @return
     *      never null.
     */
    @Nonnull
    @Exported(inline = true)
    public abstract BlueUser getAuthor();

    /**
     * Returns the timestamp of this commit.
     *
     * @return
     *      null if the implementation doesn't support it (for example, in CVS a commit
     *      spreads over time between multiple changes on multiple files, so there's no single timestamp.)
     */
    @Nullable
    @Exported
    public abstract String getTimestamp();

    /**
     * Gets the "commit message".
     *
     * @return
     *      Can be empty but never null.
     */
    @Nonnull
    @Exported
    public abstract String getMsg();

    /**
     * Returns a set of paths in the workspace that was
     * affected by this change.
     *
     * <p>
     * Contains string like 'foo/bar/zot'. No leading/trailing '/',
     * and separator must be normalized to '/'.
     *
     * @return never null.
     */
    @Nonnull
    @Exported
    public abstract Collection<String> getAffectedPaths();

    /**
     * Returns a browser friendly url to the commit.
     *
     * E.g to github, or bitbucket.
     *
     * @return null if no applicable website exists
     */
    @Nullable
    @Exported
    public abstract String getUrl();
}
