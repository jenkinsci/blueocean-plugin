/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
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
package io.jenkins.blueocean.service.embedded.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;

import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.apache.commons.lang.StringUtils;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.DELETE;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cloudbees.plugins.credentials.CredentialsProvider;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Node;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.ApiHead;
import io.jenkins.blueocean.rest.factory.organization.AbstractOrganization;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineContainer;
import io.jenkins.blueocean.rest.model.BlueUser;
import io.jenkins.blueocean.rest.model.BlueUserContainer;
import io.jenkins.blueocean.rest.model.BlueUserPermission;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
@PrepareForTest({ Jenkins.class, User.class })
public class UserImplPermissionTest {
    private TestOrganization testOrganization;
    private User user;
    private Authentication authentication;
    private Jenkins jenkins;

    @Before
    public void setup() throws IOException {
        testOrganization = new TestOrganization("org", "orgDisplayName");

        user = mock(User.class);
        when(user.getId()).thenReturn("some_user");
        authentication = new Authentication() {
            public String getName() {
                return "some_user";
            }
            public GrantedAuthority[] getAuthorities() { return null; }
            public Object getCredentials() { return null; }
            public Object getDetails() { return null; }
            public Object getPrincipal() { return null; }
            public boolean isAuthenticated() { return false; }
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {}
        };

        jenkins = mock(Jenkins.class);
        when(jenkins.getACL()).thenReturn(new ACL() {
            public boolean hasPermission(Authentication a, Permission permission) {
                return false;
            }
        });

        mockStatic(Jenkins.class);
        when(Jenkins.getAuthentication()).thenReturn(authentication);
        when(Jenkins.getInstance()).thenReturn(jenkins);

        try {
            // After Jenkins 2.77 hasPermission is no longer in Node.class and is not final so we need to mock it
            // prior to it is called as being final and mocking it will fail for the same reason.
            // TODO remove after core base line is >= 2.77
            Node.class.getDeclaredMethod("hasPermission", Permission.class);
        } catch (NoSuchMethodException e) {
            when(jenkins.hasPermission(Mockito.any())).thenAnswer(new Answer<Boolean>() {
                public Boolean answer(InvocationOnMock invocation) {
                    Permission permission = invocation.getArgument(0);
                    Jenkins j = (Jenkins) invocation.getMock();
                    ACL acl = j.getACL();
                    try {
                        return acl.hasPermission(permission);
                    } catch (NullPointerException x) {
                        throw new AssumptionViolatedException("TODO cannot be made to work prior to Spring Security update", x);
                    }
                }
            });
        }

        mockStatic(User.class);
        when(User.get("some_user", false, Collections.EMPTY_MAP)).thenReturn(user);
    }


    /**
     * Tests that the permissions are checked against the organization base and not the Jenkins instance. In this case
     * Jenkins instance will always return false to permission request and the organization base true.
     */
    @Test
    public void useTestAgainstOrgBaseOnFolder() {
        try { // https://github.com/powermock/powermock/issues/428
        UserImpl userImpl = new UserImpl(testOrganization, user, testOrganization);
        checkPermissions(userImpl.getPermission(), false, true);
        } catch (AssumptionViolatedException x) {
            System.err.println(x);
        }
    }

    /**
     * Tests against jenkins
     */
    @Test
    public void useTestAgainstJenkinsRoot() {
        try { // https://github.com/powermock/powermock/issues/428
        OrganizationImpl baseOrg = new OrganizationImpl("jenkins", jenkins);
        UserImpl userImpl = new UserImpl(baseOrg, user, baseOrg);
        checkPermissions(userImpl.getPermission(), false, false);

        when(jenkins.getACL()).thenReturn(new ACL() {
            public boolean hasPermission(Authentication a, Permission permission) {
                return true;
            }
        });

        checkPermissions(userImpl.getPermission(), true, true);
        } catch (AssumptionViolatedException x) {
            System.err.println(x);
        }
    }

    private void checkPermissions(BlueUserPermission permission, boolean shouldBeAdmin, boolean shouldHaveOtherPermissions) {

        assertEquals("User permission does not match", permission.isAdministration(), shouldBeAdmin);

        Map<String, Boolean> permissions = permission.getPipelinePermission();
        assertEquals("User permission does not match", shouldHaveOtherPermissions, permissions.get(BluePipeline.CREATE_PERMISSION));
        assertEquals("User permission does not match", shouldHaveOtherPermissions, permissions.get(BluePipeline.READ_PERMISSION));
        assertEquals("User permission does not match", shouldHaveOtherPermissions, permissions.get(BluePipeline.START_PERMISSION));
        assertEquals("User permission does not match", shouldHaveOtherPermissions, permissions.get(BluePipeline.STOP_PERMISSION));
        assertEquals("User permission does not match", shouldHaveOtherPermissions, permissions.get(BluePipeline.CONFIGURE_PERMISSION));

        permissions = permission.getCredentialPermission();
        assertEquals("User permission does not match", shouldHaveOtherPermissions, permissions.get(CredentialsProvider.CREATE.name.toLowerCase()));
        assertEquals("User permission does not match", shouldHaveOtherPermissions, permissions.get(CredentialsProvider.VIEW.name.toLowerCase()));
        assertEquals("User permission does not match", shouldHaveOtherPermissions, permissions.get(CredentialsProvider.DELETE.name.toLowerCase()));
        assertEquals("User permission does not match", shouldHaveOtherPermissions, permissions.get(CredentialsProvider.UPDATE.name.toLowerCase()));
        assertEquals("User permission does not match", shouldHaveOtherPermissions, permissions.get(StringUtils.uncapitalize(CredentialsProvider.MANAGE_DOMAINS.name)));
    }

    public static class TestOrganization extends AbstractOrganization implements ModifiableTopLevelItemGroup, AccessControlled {
        private final String name;
        private final String displayName;

        private final UserContainerImpl users = new UserContainerImpl(this, this);

        public TestOrganization(@NonNull String name, @CheckForNull String displayName) {
            this.name = name;
            this.displayName = displayName != null ? displayName : name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Nonnull
        @Override
        public ModifiableTopLevelItemGroup getGroup() {
            return this;
        }

        @Override
        public BluePipelineContainer getPipelines() {
            return new PipelineContainerImpl(this, this, this);
        }

        @WebMethod(name = "")
        @DELETE
        public void delete() {
            throw new ServiceException.NotImplementedException("Not implemented yet");
        }

        @Override
        public BlueUserContainer getUsers() {
            return users;
        }

        @Override
        public BlueUser getUser() {
            User user = User.current();
            if (user == null) {
                throw new ServiceException.NotFoundException("No authenticated user found");
            }
            return new UserImpl(this, user, new UserContainerImpl(this, this));
        }

        @Override
        public Link getLink() {
            return ApiHead.INSTANCE().getLink().rel("organizations/" + getName());
        }

        @Override
        public TopLevelItem doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            return null;
        }

        @Override
        public String getFullName() {
            return name;
        }

        @Override
        public String getFullDisplayName() {
            return name;
        }

        @Override
        public Collection<TopLevelItem> getItems() {
            return null;
        }

        @Override
        public String getUrl() {
            return null;
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
        public File getRootDir() {
            return null;
        }

        @Override
        public void save() throws IOException {
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
        public ACL getACL() {
            return null;
        }

        @Override
        public void checkPermission(Permission permission) throws AccessDeniedException {
        }

        @Override
        public boolean hasPermission(Permission permission) {
            return true;
        }
    }
}
