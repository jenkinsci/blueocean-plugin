package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import hudson.model.Run;
import io.jenkins.blueocean.commons.ServiceException.BadRequestExpception;
import io.jenkins.blueocean.commons.ServiceException.NotFoundException;
import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.rest.model.BlueTestResult;
import io.jenkins.blueocean.rest.model.BlueTestResult.Status;
import io.jenkins.blueocean.rest.model.BlueTestResultContainer;
import io.jenkins.blueocean.service.embedded.rest.BlueTestResultFactory.Result;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class BlueTestResultContainerImpl extends BlueTestResultContainer {
    private final Run<?, ?> run;

    public BlueTestResultContainerImpl(BlueRun parent, Run<?, ?> run) {
        super(parent);
        this.run = run;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlueTestResult get(final String name) {
        Result resolved = BlueTestResultFactory.resolve(run, parent);
        checkFoundTests(resolved);
        BlueTestResult testResult = Iterables.find(resolved.results, new Predicate<BlueTestResult>() {
            @Override
            public boolean apply(@Nullable BlueTestResult input) {
                return input != null && input.getId().equals(name);
            }
        }, null);
        if (testResult == null) {
            throw new NotFoundException("not found");
        }
        return testResult;
    }

    @Nonnull
    @Override
    public Iterator<BlueTestResult> iterator() {
        Result resolved = BlueTestResultFactory.resolve(run, parent);
        checkFoundTests(resolved);
        StaplerRequest request = Stapler.getCurrentRequest();
        if (request != null) {
            String status = request.getParameter("status");
            String[] atoms = StringUtils.split(status, ',');
            Predicate<BlueTestResult> predicate = Predicates.alwaysFalse();
            if (atoms != null && atoms.length > 0) {
                for (String statusString : atoms) {
                    Status queryStatus;
                    try {
                        queryStatus = Status.valueOf(statusString.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestExpception("bad status " + status, e);
                    }
                    predicate = Predicates.or(predicate, new StatusPredicate(queryStatus));
                }
                return Iterables.filter(resolved.results, predicate).iterator();
            }
        }
        return resolved.results.iterator();
    }

    private void checkFoundTests(Result resolved) {
        if (resolved.summary.getTotal() == 0) {
            throw new NotFoundException("no tests");
        }
    }

    class StatusPredicate implements Predicate<BlueTestResult> {

        private final Status status;

        StatusPredicate(Status status) {
            this.status = status;
        }

        @Override
        public boolean apply(@Nullable BlueTestResult input) {
            return input != null && input.getStatus().equals(status);
        }
    }
}
