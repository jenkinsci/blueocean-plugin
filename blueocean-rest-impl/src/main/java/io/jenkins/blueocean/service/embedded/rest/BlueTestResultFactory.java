package io.jenkins.blueocean.service.embedded.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import hudson.ExtensionPoint;
import hudson.model.Run;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.model.BlueTestResult;
import io.jenkins.blueocean.rest.model.BlueTestSummary;
import jenkins.model.Jenkins;

import javax.annotation.Nullable;

public abstract class BlueTestResultFactory implements ExtensionPoint {

    /**
     * @param run to find tests for
     * @param parent run that this belongs to
     * @return implementation of BlueTestResult matching your TestResult or {@link Result#notFound()}
     */
    public abstract Result getBlueTestResults(Run<?, ?> run, final Reachable parent);

    /**
     * Result of {@link #getBlueTestResults(Run, Reachable)} that holds summary and iterable of BlueTestResult
     */
    public static final class Result {

        private static final Result NOT_FOUND = new Result(ImmutableList.<BlueTestResult>of(), null);

        @Nullable
        public final Iterable<BlueTestResult> results;
        @Nullable
        public final BlueTestSummary summary;

        private Result(Iterable<BlueTestResult> results, BlueTestSummary summary) {
            this.results = results;
            this.summary = summary;
        }

        /**
         * @param results to report
         * @param summary pre-calculated summary information
         * @return result
         */
        public static Result of(Iterable<BlueTestResult> results, BlueTestSummary summary) {
            return new Result(results, summary);
        }

        /**
         * Calculates a summary from the provided results
         * @param results to report
         * @return result
         */
        public static Result of(Iterable<BlueTestResult> results) {
            long skipped = 0;
            long passed = 0;
            long failed = 0;
            long regressions = 0;
            long existingFailedTotal = 0;
            long fixedTotal = 0;
            long total = 0;
            for (BlueTestResult result : results) {
                switch (result.getStatus()) {
                    case SKIPPED:
                        skipped++;
                        break;
                    case PASSED:
                        passed++;
                        switch (result.getTestState()) {
                            case FIXED:
                                fixedTotal++;
                                break;
                        }
                        break;
                    case FAILED:
                        failed++;
                        switch (result.getTestState()) {
                            case REGRESSION:
                                regressions++;
                                break;
                            default:
                                existingFailedTotal++;
                                break;
                        }
                        break;
                }
                total++;
            }
            if (total == 0) {
                return notFound();
            } else {
                BlueTestSummary summary = new BlueTestSummary(passed, failed, fixedTotal, existingFailedTotal, regressions, skipped, total);
                return new Result(results, summary);
            }
        }

        /**
         * @return no results found
         */
        public static Result notFound() {
            return NOT_FOUND;
        }
    }

    public static Result resolve(Run<?, ?> run, Reachable parent) {
        Iterable<BlueTestResult> results = ImmutableList.of();
        BlueTestSummary summary = new BlueTestSummary(0, 0, 0, 0, 0, 0, 0);
        for (BlueTestResultFactory factory : Jenkins.getInstance().getExtensionList(BlueTestResultFactory.class)) {
            Result result = factory.getBlueTestResults(run, parent);
            if (result != null && result.results != null && result.summary != null) {
                results = Iterables.concat(result.results, results);
                summary = summary.tally(result.summary);
            }
        }
        // Never send back an empty summary
        if (summary.getTotal() == 0) {
            summary = null;
            results = null;
        }
        return Result.of(results, summary);
    }
}
