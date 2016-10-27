package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.model.ItemGroup;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipelineFolder;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public interface ScmProvider {
    BluePipelineFolder createProjects(MultiBranchPipelineImpl.ScmOrganizationPipelineRequest request, ItemGroup parent, Link parentLink) throws IOException;
}
