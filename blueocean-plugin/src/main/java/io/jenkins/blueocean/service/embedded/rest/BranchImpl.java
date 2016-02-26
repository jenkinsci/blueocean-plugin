package io.jenkins.blueocean.service.embedded.rest;

import io.jenkins.blueocean.rest.model.BlueBranch;

/**
 * @author Vivek Pandey
 */
public class BranchImpl extends BlueBranch {

    private final String branch;

    public BranchImpl(String branch) {
        this.branch = branch;
    }

    @Override
    public String getName() {
        return branch;
    }
}
