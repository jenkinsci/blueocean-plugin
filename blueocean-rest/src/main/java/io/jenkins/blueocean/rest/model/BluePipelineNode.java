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
    public static final String DISPLAY_NAME="displayName";
    public static final String RESULT = "result";
    public static final String START_TIME="startTime";
    public static final String ID = "id";
    public static final String EDGES = "edges";
    public static final String DURATION_IN_MILLIS="durationInMillis";

    @Exported(name = ID)
    public abstract String getId();

    @Exported(name = DISPLAY_NAME)
    public abstract String getDisplayName();

    @Exported(name = RESULT)
    public abstract BlueRun.BlueRunResult getResult();

    @Exported(name=STATE)
    public abstract BlueRun.BlueRunState getStateObj();

    public abstract Date getStartTime();

    @Exported(name = EDGES, inline = true)
    public abstract List<Edge> getEdges();

    @Exported(name = START_TIME)
    public final String getStartTimeString(){
        if(getStartTime() == null) {
            return null;
        }
        return new SimpleDateFormat(BlueRun.DATE_FORMAT_STRING).format(getStartTime());
    }

    @Exported(name= DURATION_IN_MILLIS)
    public abstract Long getDurationInMillis();

    /**
     * @return Gives logs associated with this node
     */
    public abstract Object getLog();

    @ExportedBean
    public abstract static class Edge{
        @Exported
        public abstract String getId();
    }

    /**
     * @return Steps inside a Pipeline Stage or Parallel branch
     */
    public abstract BluePipelineStepContainer getSteps();
}
