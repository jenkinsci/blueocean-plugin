package io.jenkins.blueocean.indexing;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.Job;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.EmbeddedRunLoader;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.SortedNumericSortField;

import java.io.IOException;

import static io.jenkins.blueocean.indexing.Fields.sortField;

@Extension
public class RunLoaderImpl extends EmbeddedRunLoader {

    @Inject
    IndexService index;

    @Override
    public Iterable<BlueRun> getRuns(Job job, Link parent) {
        Index<BlueRun> runIndex = index.getIndex(job);
        try {
            return runIndex.query(
                new MatchAllDocsQuery(),
                100, new Sort(new SortedNumericSortField(sortField(BlueRun.START_TIME), Type.LONG, true)),
                Transformers.documentToRun(parent)
            );
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Could not load runs", e);
        }
    }
}
