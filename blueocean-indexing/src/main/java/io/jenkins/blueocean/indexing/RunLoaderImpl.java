package io.jenkins.blueocean.indexing;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.RunLoader;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueRun;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TermQuery;

import java.io.IOException;

@Extension
public class RunLoaderImpl extends RunLoader {

    @Inject
    IndexService index;

    @Override
    public Iterable<BlueRun> getRuns(Job job, Link parent) {
        Index<BlueRun> runIndex = index.getIndex(job);
        try {
            return runIndex.query(new MatchAllDocsQuery(), 100, new Sort(new SortField(BlueRun.START_TIME, Type.LONG)), Transformers.documentToRun(parent));
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Could not load runs", e);
        }
    }

    @Override
    public BlueRun getRun(String id, Job job, Link parent) {
        Index<BlueRun> runIndex = index.getIndex(job);
        try {
            return Iterables.getOnlyElement(runIndex.query(new TermQuery(Terms.runId(id)), 1, null, Transformers.documentToRun(parent)));
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Could not load runs", e);
        }
    }
}
