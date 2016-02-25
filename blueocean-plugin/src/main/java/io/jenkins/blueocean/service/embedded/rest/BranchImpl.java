package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.model.BOBranch;

/**
 * @author Vivek Pandey
 */
public class BranchImpl extends BOBranch {

    private final String branch;

    public BranchImpl(String branch) {
        this.branch = branch;
    }

    @Override
    public String getName() {
        return branch;
    }
}
