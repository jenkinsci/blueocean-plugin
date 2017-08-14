package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueTestResultFactory;
import io.jenkins.blueocean.rest.model.BlueTestSummary;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

final class Caches {

    private static final long TEST_SUMMARY_CACHE_MAX_SIZE = Long.getLong("TEST_SUMMARY_CACHE_MAX_SIZE", 10000);

    private static final Cache<String, Optional<BlueTestSummary>> TEST_SUMMARY = CacheBuilder.newBuilder()
        .maximumSize(TEST_SUMMARY_CACHE_MAX_SIZE)
        .expireAfterAccess(1, TimeUnit.DAYS)
        .build();

    static Optional<BlueTestSummary> loadTestSummary(final Run run, final Reachable parent) {
        try {
            return TEST_SUMMARY.get(run.getExternalizableId(), new Callable<Optional<BlueTestSummary>>() {
                @Override
                public Optional<BlueTestSummary> call() throws Exception {
                    BlueTestSummary summary = BlueTestResultFactory.resolve(run, parent).summary;
                    return summary == null ? Optional.<BlueTestSummary>absent() : Optional.of(summary);
                }
            });
        } catch (ExecutionException e) {
            return Optional.absent();
        }
    }
}
