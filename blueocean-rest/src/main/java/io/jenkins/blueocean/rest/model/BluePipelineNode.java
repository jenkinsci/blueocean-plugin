package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static io.jenkins.blueocean.rest.model.BlueRun.STATE;

/**
 * Abstraction of Pipeline run node.
 *
 * This node is a node in Pipeline run and a vetext in DAG. Each sub-steps in this pipeline node is
 * represented as edges.
 *
 * e.g.
 * <pre>
 * stage 'build'
 * node{
 *   echo "Building..."
 * }
 * stage 'test'
 * parallel 'unit':{
 *   node{
 *     echo "Unit testing..."
 *   }
 * },'integration':{
 *   node{
 *     echo "Integration testing..."
 *   }
 * }
 * stage 'deploy'
 * node{
 * echo "Deploying"
 * }
 *</pre>
 *
 * Above pipeline script is modeled as:
 *
 *  <pre>
 * build : test
 * test : unit, integration
 * unit : deploy
 * integration: deploy
 * deploy
 *
 *
 *                   /---- unit ----------\
 * build---&gt;test---&gt;/                     \------&gt; deploy
 *                  \----- integration ---/
 *
 *</pre>
 * @author Vivek Pandey
 */
public abstract class BluePipelineNode extends Resource{

    @Exported
    public abstract String getId();

    @Exported
    public abstract String getDisplayName();

    @Exported
    public abstract BlueRun.BlueRunResult getResult();

    @Exported(name=STATE)
    public abstract BlueRun.BlueRunState getStateObj();

    public abstract Date getStartTime();

    @Exported(inline = true)
    public abstract List<Edge> getEdges();

    @Exported(name = "startTime")
    public final String getStartTimeString(){
        return new SimpleDateFormat(BlueRun.DATE_FORMAT_STRING).format(getStartTime());
    }

    /**
     * @return Gives logs associated with this node
     */
    public abstract Object getLog();

    @ExportedBean
    public abstract static class Edge{
        @Exported
        public abstract String getId();

        @Exported
        public abstract long getDurationInMillis();
    }

}
