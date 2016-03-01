package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.CustomExportedBean;
import org.kohsuke.stapler.export.Exported;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public abstract class BlueChangeLog extends Resource {

    @Exported(name = "commitId")
    public abstract String getCommitId();

    @Exported(name = "author")
    public abstract BlueUser getAuthor();


    @Exported(name = "timestamp")
    public abstract Date getTimeStamp();

    @Exported(name = "commitMessage")
    public abstract String getCommitMessage();

    @Exported(name = "affectedPaths")
    public abstract Iterator<String> getAffectedPaths();

    @Exported(name = "affectedFiles")
    public abstract Iterator<AffectedFile> getAffectedFiles();

    /**
     * Represents a file change. Contains filename, edit type, etc.
     *
     * I checked the API names against some some major SCMs and most SCMs
     * can adapt to this interface with very little changes
     *
     * @see #getAffectedFiles()
     */
    public interface AffectedFile {
        /**
         * The path in the workspace that was affected
         * <p>
         * Contains string like 'foo/bar/zot'. No leading/trailing '/',
         * and separator must be normalized to '/'.
         *
         * @return never null.
         */
        String getPath();


        /**
         * Return whether the file is new/modified/deleted
         */
        EditType getEditType();
    }

    /**
     * Designates the SCM operation.
     *
     * @author Kohsuke Kawaguchi
     */
    public final static class EditType implements CustomExportedBean {
        private String name;
        private String description;

        public EditType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String toExportedObject() {
            return name;
        }

        public static final EditType ADD = new EditType("add","The file was added");
        public static final EditType EDIT = new EditType("edit","The file was modified");
        public static final EditType DELETE = new EditType("delete","The file was removed");

        public static final List<EditType> ALL = Collections.unmodifiableList(Arrays.asList(ADD,EDIT,DELETE));
    }

}
