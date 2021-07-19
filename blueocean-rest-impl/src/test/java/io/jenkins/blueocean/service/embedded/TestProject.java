package io.jenkins.blueocean.service.embedded;

import hudson.Extension;
import hudson.model.Build;
import hudson.model.ItemGroup;
import hudson.model.Project;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;
import jenkins.model.item_category.StandaloneProjectsCategory;

import java.io.File;
import java.io.IOException;

/**
 * Test Project
 * @author Vivek Pandey
 */
public class TestProject extends Project<TestProject,TestProject.TestBuild> implements TopLevelItem {

    public TestProject(ItemGroup parent, String name) {
        super(parent, name);
    }

    @Override
    protected Class<TestBuild> getBuildClass() {
        return TestBuild.class;
    }

    public TestProject.DescriptorImpl getDescriptor() {
        return (TestProject.DescriptorImpl) Jenkins.get().getDescriptorOrDie(getClass());
    }


    @Extension
//    @Symbol({"testProjectBOTest","testProjectBOTestJob"})
    public static class DescriptorImpl extends AbstractProjectDescriptor {

        public DescriptorImpl() {

        }

        public String getDisplayName() {
            return "TestProject";
        }


        @Override
        public String getDescription() {
            return "TestProject";
        }

        @Override
        public String getCategoryId() {
            return StandaloneProjectsCategory.ID;
        }

        @Override
        public String getIconFilePathPattern() {
            return (Jenkins.RESOURCE_PATH + "/images/:size/freestyleproject.png").replaceFirst("^/", "");
        }


        @Override
        public TestProject newInstance(ItemGroup parent, String name) {
            return new TestProject(parent,name);
        }

    }

    public static class TestBuild extends Build<TestProject,TestBuild> {
        public TestBuild(TestProject project) throws IOException {
            super(project);
        }

        public TestBuild(TestProject project, File buildDir) throws IOException {
            super(project, buildDir);
        }

    }

}
