package io.jenkins.blueocean.rest.impl.pipeline;

import hudson.Extension;
import io.jenkins.blueocean.rest.factory.BlueTrendFactory;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineNode;
import io.jenkins.blueocean.rest.model.BluePipelineStep;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueRunContainer;
import io.jenkins.blueocean.rest.model.BlueTableRow;
import io.jenkins.blueocean.rest.model.BlueTrend;
import io.jenkins.blueocean.rest.model.Container;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Trend for Stage durations
 */
public class StageDurationTrend extends BlueTrend {

    private final PipelineImpl pipeline;
    private final Link parent;

    public StageDurationTrend(PipelineImpl pipeline, Link parent) {
        this.pipeline = pipeline;
        this.parent = parent;
    }

    @Override
    public String getId() {
        return "stageDuration";
    }

    @Override
    public String getDisplayName() {
        return "Stage Duration";
    }

    @Override
    public Map<String, String> getColumns() {
        return Collections.emptyMap();
    }

    @Override
    public Link getLink() {
        return parent.rel(getId());
    }

    @Override
    public Container<BlueTableRow> getRows() {
        BlueRunContainer blueRunContainer = pipeline.getRuns();

        return new Container<BlueTableRow>() {
            @Override
            public Link getLink() {
                return parent.rel("rows");
            }

            @Override
            public BlueTableRow get(String name) {
                return null;
            }

            @Override
            public Iterator<BlueTableRow> iterator() {
                return blueRunContainer == null
                    ? null
                    : StreamSupport.stream(blueRunContainer.spliterator(), false ).
                        map(blueRun -> (BlueTableRow)new StageDurationTrendRow(blueRun)).iterator();
            }
        };
    }

    @ExportedBean(defaultVisibility = 1000)
    public static class StageDurationTrendRow extends BlueTableRow {

        static final String NODES = "nodes";

        private final BlueRun run;

        public StageDurationTrendRow(BlueRun run) {
            this.run = run;
        }

        @Override
        public String getId() {
            return run.getId();
        }

        // TODO: doesn't work, node properties are omitted
        // @Exported(merge = true)
        @Exported(name = NODES)
        public Map getNodes() {
            return StreamSupport.stream(run.getNodes().spliterator(), false)
                .collect(Collectors.toMap(BluePipelineNode::getDisplayName, BluePipelineStep::getDurationInMillis));
        }

        /*
        TODO: doesn't work, method is never called
        @Override
        public Object toExportedObject() {
            ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            builder.put("id", getId());
            for (BluePipelineNode node : run.getNodes()) {
                builder.put(node.getDisplayName(), node.getDurationInMillis());
            }
            return builder.build();
        }
        */
    }

    @Extension
    public static class FactoryImpl extends BlueTrendFactory {
        @Override
        public BlueTrend getTrend(BluePipeline pipeline, Link parent) {
            if (!(pipeline instanceof PipelineImpl)) {
                return null;
            }
            PipelineImpl pipelineImpl = (PipelineImpl)pipeline;
            return new StageDurationTrend(pipelineImpl, parent);
        }
    }
}
