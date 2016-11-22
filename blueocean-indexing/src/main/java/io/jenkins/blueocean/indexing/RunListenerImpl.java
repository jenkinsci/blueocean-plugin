package io.jenkins.blueocean.indexing;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl;
import io.jenkins.blueocean.service.embedded.rest.BluePipelineFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class RunListenerImpl extends RunListener<Run<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(RunListenerImpl.class.getName());

    @Inject
    private IndexService indexes;

    @Override
    public void onFinalized(Run<?, ?> run) {
        Index<BlueRun> index = indexes.getIndex(IndexStrategy.find(run));

        Reachable parent = BluePipelineFactory.resolve(run.getParent());
        BlueRun blueRun = AbstractRunImpl.getBlueRun(run, parent);
        try {
            index.addDocuments(ImmutableList.of(blueRun), Transformers.RUN_TO_DOCUMENT);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not add run to index", e);
        }
    }

    @Override
    public void onDeleted(Run<?, ?> run) {
        Index<BlueRun> index = indexes.getIndex(IndexStrategy.find(run));
        try {
            index.delete(Terms.runId(run));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not add run to index", e);
        }
    }

}
