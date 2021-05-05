/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
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
package io.jenkins.blueocean.service.embedded;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import hudson.model.ModelObject;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import io.jenkins.blueocean.rest.factory.BlueOceanUrlMapper;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.service.embedded.rest.OrganizationImpl;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.acegisecurity.AccessDeniedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BlueOceanWebURLBuilderTest {
    private BlueOceanUrlMapper urlMapper;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setup(){
        assertTrue(BlueOceanUrlMapper.all().size() > 0);
        urlMapper = BlueOceanUrlMapper.all().get(0);
    }

    @Test
    public void test_freestyle() throws IOException, ExecutionException, InterruptedException {
        MockFolder folder1 = jenkinsRule.createFolder("folder1");
        MockFolder folder2 = folder1.createProject(MockFolder.class, "folder two with spaces");
        FreeStyleProject freestyleProject = folder2.createProject(FreeStyleProject.class, "freestyle with spaces");
        String blueOceanURL;

        blueOceanURL = urlMapper.getUrl(freestyleProject);
        assertURL("blue/organizations/jenkins/folder1%2Ffolder%20two%20with%20spaces%2Ffreestyle%20with%20spaces/", blueOceanURL);

        FreeStyleBuild run = freestyleProject.scheduleBuild2(0).get();
        blueOceanURL = urlMapper.getUrl(run);
        assertURL("blue/organizations/jenkins/folder1%2Ffolder%20two%20with%20spaces%2Ffreestyle%20with%20spaces/detail/freestyle%20with%20spaces/1/", blueOceanURL);
    }


    public static class TestOrg implements ModifiableTopLevelItemGroup{
        static TestOrg INSTANCE = new TestOrg();
        private TestOrg() {
        }

        @Override
        public <T extends TopLevelItem> T copy(T src, String name) throws IOException {
            return null;
        }

        @Override
        public TopLevelItem createProjectFromXML(String name, InputStream xml) throws IOException {
            return null;
        }

        @Override
        public TopLevelItem createProject(TopLevelItemDescriptor type, String name, boolean notify) throws IOException {
            return null;
        }

        @Override
        public TopLevelItem doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            return null;
        }

        @Override
        public String getFullName() {
            return "testorg";
        }

        @Override
        public String getFullDisplayName() {
            return "testorg";
        }

        @Override
        public Collection<TopLevelItem> getItems() {
            return null;
        }

        @Override
        public String getUrl() {
            return "testorg";
        }

        @Override
        public String getUrlChildPrefix() {
            return null;
        }

        @Override
        public TopLevelItem getItem(String name) throws AccessDeniedException {
            return null;
        }

        @Override
        public File getRootDirFor(TopLevelItem child) {
            return null;
        }

        @Override
        public void onRenamed(TopLevelItem item, String oldName, String newName) throws IOException {

        }

        @Override
        public void onDeleted(TopLevelItem item) throws IOException {

        }

        @Override
        public String getDisplayName() {
            return "testorg";
        }

        @Override
        public File getRootDir() {
            return null;
        }

        @Override
        public void save() throws IOException {

        }
    }

    @TestExtension(value = "testOrg")
    public static class TestOrganizationFactoryImpl extends OrganizationFactoryImpl {
        private OrganizationImpl instance = new OrganizationImpl("testorg", TestOrg.INSTANCE);

        @Override
        public OrganizationImpl get(String name) {
            if (instance != null) {
                if (instance.getName().equals(name)) {
                    return instance;
                }
            }
            return null;
        }

        @Override
        public Collection<BlueOrganization> list() {
            return Collections.singleton((BlueOrganization) instance);
        }

        @Override
        public OrganizationImpl of(ItemGroup group) {
            if (group == instance.getGroup()) {
                return instance;
            }
            return null;
        }
    }

    @Test
    public void testOrg() throws IOException, ExecutionException, InterruptedException {
        String blueOceanURL = urlMapper.getUrl(TestOrg.INSTANCE);
        assertURL("blue/organizations/testorg", blueOceanURL);
    }

    @TestExtension("testCustomerUrlMapper")
    public static class TestUrlMapper extends BlueOceanUrlMapper{

        @Override
        public String getUrl(@Nonnull ModelObject modelObject) {
            return modelObject instanceof FreeStyleProject ? "/customerUrlMapper/"+((FreeStyleProject)modelObject).getName() : null;
        }
    }
    @Test
    public void testCustomerUrlMapper() throws Exception{
        FreeStyleProject freestyleProject = jenkinsRule.createProject(FreeStyleProject.class, "freestyle1");
        String url = urlMapper.getUrl(freestyleProject);
        assertEquals("/customerUrlMapper/freestyle1", url);
    }



    private void assertURL(String expected, String actual) throws IOException {
        Assert.assertEquals(expected, actual);
    }
}
