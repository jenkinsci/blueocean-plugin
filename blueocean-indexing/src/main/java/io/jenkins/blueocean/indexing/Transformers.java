package io.jenkins.blueocean.indexing;

import com.google.common.base.Function;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueActionProxy;
import io.jenkins.blueocean.rest.model.BlueChangeSetEntry;
import io.jenkins.blueocean.rest.model.BluePipelineNodeContainer;
import io.jenkins.blueocean.rest.model.BluePipelineStepContainer;
import io.jenkins.blueocean.rest.model.BlueQueueItem;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.Container;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Date;

import static io.jenkins.blueocean.indexing.Fields.sortField;

final class Transformers {

    public static Function<Document, BlueRun> documentToRun(final Link parent) {
        return new Function<Document, BlueRun>() {
            @Nullable
            @Override
            public BlueRun apply(@Nullable final Document input) {
                return new BlueRun() {
                    @Override
                    public String getOrganization() {
                        return input.get(BlueRun.ORGANIZATION);
                    }

                    @Override
                    public String getId() {
                        return input.get(BlueRun.ID);
                    }

                    @Override
                    public String getPipeline() {
                        return input.get(BlueRun.PIPELINE);
                    }

                    @Override
                    public Date getStartTime() {
                        return new Date(input.getField(BlueRun.START_TIME).numericValue().longValue());
                    }

                    @Override
                    public Container<BlueChangeSetEntry> getChangeSet() {
                        return null;
                    }

                    @Override
                    public Date getEnQueueTime() {
                        return new Date(input.getField(BlueRun.ENQUEUE_TIME).numericValue().longValue());
                    }

                    @Override
                    public Date getEndTime() {
                        return new Date(input.getField(BlueRun.END_TIME).numericValue().longValue());
                    }

                    @Override
                    public Long getDurationInMillis() {
                        return input.getField(BlueRun.DURATION_IN_MILLIS).numericValue().longValue();
                    }

                    @Override
                    public Long getEstimatedDurtionInMillis() {
                        return input.getField(BlueRun.ESTIMATED_DURATION_IN_MILLIS).numericValue().longValue();
                    }

                    @Override
                    public BlueRunState getStateObj() {
                        return null;
                    }

                    @Override
                    public BlueRunResult getResult() {
                        return BlueRunResult.valueOf(input.get(BlueRun.RESULT));
                    }

                    @Override
                    public String getRunSummary() {
                        return input.get(BlueRun.RUN_SUMMARY);
                    }

                    @Override
                    public String getType() {
                        return input.get(BlueRun.TYPE);
                    }

                    @Override
                    public BlueRun stop(@QueryParameter("blocking") Boolean blocking, @QueryParameter("timeOutInSecs") Integer timeOutInSecs) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Container<BlueArtifact> getArtifacts() {
                        return null;
                    }

                    @Override
                    public BluePipelineNodeContainer getNodes() {
                        return null;
                    }

                    @Override
                    public Collection<BlueActionProxy> getActions() {
                        return null;
                    }

                    @Override
                    public BluePipelineStepContainer getSteps() {
                        return null;
                    }

                    @Override
                    public Object getLog() {
                        return null;
                    }

                    @Override
                    public BlueQueueItem replay() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Link getLink() {
                        return parent.rel("runs/"+getId());
                    }
                };
            }
        };
    }

    public static final Function<BlueRun, Document> RUN_TO_DOCUMENT = new Function<BlueRun, Document>() {
        @Nullable
        @Override
        public Document apply(BlueRun input) {
            Document document = new Document();
            document.add(new LongField(BlueRun.ID, Integer.valueOf(input.getId()), Store.YES));
            document.add(new NumericDocValuesField(sortField(BlueRun.ID), Integer.valueOf(input.getId())));
            document.add(new StringField(BlueRun.RESULT, input.getResult().name(), Store.YES));
            document.add(new StringField(BlueRun.ORGANIZATION, input.getOrganization(), Store.YES));
            document.add(new StringField(BlueRun.PIPELINE, input.getPipeline(), Store.YES));
            document.add(new StringField(BlueRun.TYPE, input.getType(), Store.YES));
            document.add(new StringField(BlueRun.RUN_SUMMARY, input.getRunSummary(), Store.YES));
            document.add(new LongField(BlueRun.START_TIME, input.getStartTime().getTime(), Store.YES));
            document.add(new LongField(BlueRun.END_TIME, input.getEndTime().getTime(), Store.YES));
            document.add(new LongField(BlueRun.ENQUEUE_TIME, input.getEnQueueTime().getTime(), Store.YES));
            document.add(new LongField(BlueRun.DURATION_IN_MILLIS, input.getDurationInMillis(), Store.YES));
            document.add(new LongField(BlueRun.ESTIMATED_DURATION_IN_MILLIS, input.getEstimatedDurtionInMillis(), Store.YES));
            document.add(new NumericDocValuesField(sortField(BlueRun.START_TIME), input.getStartTime().getTime()));
            document.add(new NumericDocValuesField(sortField(BlueRun.END_TIME), input.getEndTime().getTime()));
            document.add(new NumericDocValuesField(sortField(BlueRun.ENQUEUE_TIME) + "_sort", input.getEnQueueTime().getTime()));
            document.add(new NumericDocValuesField(sortField(BlueRun.DURATION_IN_MILLIS) + "_sort", input.getDurationInMillis()));
            document.add(new NumericDocValuesField(sortField(BlueRun.ESTIMATED_DURATION_IN_MILLIS) + "_sort", input.getEstimatedDurtionInMillis()));
            return document;
        }
    };

    private Transformers() {}
}
