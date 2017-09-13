package io.jenkins.blueocean.rest.factory;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Job;
import hudson.scm.ChangeLogSet;
import io.jenkins.blueocean.rest.model.BlueIssue;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BlueIssueFactory implements ExtensionPoint {


    private static final Logger LOGGER = Logger.getLogger(BlueIssueFactory.class.getName());
    /**
     * @see #resolve(Job)
     * @param job job
     * @return issues
     */
    public abstract Collection<BlueIssue> getIssues(Job job);

    /**
     * @see #resolve(ChangeLogSet.Entry)
     * @param changeSetEntry entry
     * @return issues
     */
    public abstract Collection<BlueIssue> getIssues(ChangeLogSet.Entry changeSetEntry);

    /**
     * Find issues representing this job.
     * e.g. a feature branch could take the format of "feature/TICKET-123" and be represented by TICKET-123 in JIRA
     * Typically there is only one associated ticket
     *
     * @param job to find issues for
     * @return issues representing this job
     */
    public static Collection<BlueIssue> resolve(Job job) {
        LinkedHashSet<BlueIssue> allIssues = Sets.newLinkedHashSet();
        for (BlueIssueFactory factory : ExtensionList.lookup(BlueIssueFactory.class)) {
            try {
                Collection<BlueIssue> issues = factory.getIssues(job);
                if (issues == null) {
                    continue;
                }
                allIssues.addAll(issues);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,"Unable to fetch issues for job " + e.getMessage(), e);
            }
        }
        return allIssues;
    }

    /**
     * Finds any issues associated with the changeset
     * e.g. a commit message could be "TICKET-123 fix all the things" and be associated with TICKET-123 in JIRA
     * @param changeSetEntry entry
     * @return issues representing the change
     */
    public static Collection<BlueIssue> resolve(ChangeLogSet.Entry changeSetEntry) {
        LinkedHashSet<BlueIssue> allIssues = Sets.newLinkedHashSet();
        for (BlueIssueFactory factory : ExtensionList.lookup(BlueIssueFactory.class)) {
            try {
                Collection<BlueIssue> issues = factory.getIssues(changeSetEntry);
                if (issues == null) {
                    continue;
                }
                allIssues.addAll(issues);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,"Unable to fetch issues for changeSetEntry " + e.getMessage(), e);
            }
        }
        return allIssues;
    }
}
