package io.jenkins.blueocean.service.embedded;

import com.google.common.base.Predicate;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Project;
import io.jenkins.blueocean.service.embedded.rest.ContainerFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*"})
@PrepareForTest(Stapler.class)
public class ContainerFilterTest extends BaseTest{

    @Test
    public void testPagedFilter() throws IOException {
        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getParameter("filter")).thenReturn("itemgroup-only");
        mockStatic(Stapler.class);
        when(Stapler.getCurrentRequest()).thenReturn(request);

        List<Item> items = new ArrayList<>();
        MockFolder folder = j.createFolder("folder");
        for(int i=0; i<50; i++){
            FreeStyleProject job = folder.createProject(FreeStyleProject.class, "job"+i);
            items.add(folder.createProject(MockFolder.class, "subFolder"+i));
            items.add(job);
        }
        assertEquals(100, items.size());
        //there are total 50 folders in items, we want 25 of them starting 25th ending at 49th.
        Collection<Item> jobs = ContainerFilter.filter(items, 25, 25);
        assertEquals(25, jobs.size());
        int i = 25;
        for(Item item: jobs){
            assertTrue(item instanceof ItemGroup);
            assertEquals("subFolder"+i++, item.getName());
        }
    }

    @Test
    public void customFilter() throws IOException {
        MockFolder folder = j.createFolder("folder1");
        Project p = folder.createProject(FreeStyleProject.class, "test1");
        Collection<Item> items = ContainerFilter.filter(j.getInstance().getAllItems(), "itemgroup-only");
        assertEquals(1, items.size());
        assertEquals(folder.getFullName(), items.iterator().next().getFullName());
    }

    @TestExtension
    public static class ItemGroupFilter extends ContainerFilter {

        private final Predicate<Item> filter = new Predicate<Item>() {
            @Override
            public boolean apply(Item job) {
                return (job instanceof ItemGroup);
            }
        };
        @Override
        public String getName() {
            return "itemgroup-only";
        }

        @Override
        public Predicate<Item> getFilter() {
            return filter;
        }
    }


}
