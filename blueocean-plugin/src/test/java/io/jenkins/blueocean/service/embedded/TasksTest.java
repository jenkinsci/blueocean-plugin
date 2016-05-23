package io.jenkins.blueocean.service.embedded;

import com.google.common.collect.ImmutableList;
import hudson.model.FreeStyleBuild;
import hudson.model.Project;
import hudson.tasks.Shell;
import org.apache.http.annotation.Immutable;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Ivan Meredith
 */
public class TasksTest extends BaseTest {

    @Test
    public void tasksTest() throws Exception {

        Project p1 = j.createFreeStyleProject("pipeline1");
        Project p2 = j.createFreeStyleProject("pipeline2");
        Project p3 = j.createFreeStyleProject("pipeline3");

        p1.getBuildersList().add(new Shell("echo hello!\nsleep 600"));
        p1.scheduleBuild2(0);

        p2.getBuildersList().add(new Shell("echo hello!\nsleep 600"));
        p2.scheduleBuild2(0);

        p3.getBuildersList().add(new Shell("echo hello!\nsleep 600"));
        p3.scheduleBuild2(0);

        List body = request().get("/tasks").build(List.class);
        int running = 0;
        int queued = 0;
        for (Object o : body) {
            String type = (String)((Map)o).get("type");
            if(type.equalsIgnoreCase("RUN")) {
                running++;
            }
            if(type.equalsIgnoreCase("QUEUE_ITEM")) {
                queued++;
            }
        }

        Assert.assertEquals(queued,1);
        Assert.assertEquals(running,2);
    }

    @Test
    public void getTaskTest() throws Exception {

        Project p1 = j.createFreeStyleProject("pipeline1");
        Project p2 = j.createFreeStyleProject("pipeline2");
        Project p3 = j.createFreeStyleProject("pipeline3");

        p1.getBuildersList().add(new Shell("echo hello!\nsleep 600"));
        p1.scheduleBuild2(0);

        p2.getBuildersList().add(new Shell("echo hello!\nsleep 600"));
        p2.scheduleBuild2(0);

        p3.getBuildersList().add(new Shell("echo hello!\nsleep 600"));
        p3.scheduleBuild2(0);

        for(Project p : ImmutableList.of(p1,p2,p3)) {
            long id;
            if( p.getQueueItem() == null) {
                id = p.getLastBuild().getQueueId();
            } else {
                id =  p.getQueueItem().getId();
            }

            Map r = request().get("/tasks/" + id).build(Map.class);
            Assert.assertEquals(r.get("id"), Long.toString(id));
        }

    }
}
