package io.jenkins.blueocean.indexing;

import hudson.model.Run;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.apache.lucene.index.Term;

class Terms {
    public static Term runId(Run<?, ?> run) {
        return new Term(BlueRun.ID, Integer.toString(run.getNumber()));
    }

    public static Term runId(String id) {
        return new Term(BlueRun.ID, id);
    }

    private Terms() {}
}
