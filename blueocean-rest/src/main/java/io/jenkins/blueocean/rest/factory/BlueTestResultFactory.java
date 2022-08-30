package io.jenkins.blueocean.rest.factory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Run;
import io.jenkins.blueocean.commons.IterableUtils;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.hal.Link;
import io.jenkins.blueocean.rest.model.BlueTestResult;
import io.jenkins.blueocean.rest.model.BlueTestSummary;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;

public abstract class BlueTestResultFactory implements ExtensionPoint {

    /**
     * @param run to find tests for
     * @param parent run or node that this belongs to
     * @return implementation of BlueTestResult matching your TestResult or {@link Result#notFound()}
     */
    public Result getBlueTestResults(Run<?,?> run, final Reachable parent) {
        return Result.notFound();
    }

    /**
     * Result of {@link #getBlueTestResults(Run, Reachable)} that holds summary and iterable of BlueTestResult
     */
    public static final class Result {

        private static final Result NOT_FOUND = new Result(Collections.emptyList(), null);

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
        @SuppressFBWarnings(value = "SF_SWITCH_NO_DEFAULT", justification = "Its ok in this case")
        public static Result of(Iterable<BlueTestResult> results) {
            long skipped = 0;
            long passed = 0;
            long failed = 0;
            long regressions = 0;
            long existingFailedTotal = 0;
            long fixedTotal = 0;
            long total = 0;
            Link parent = null;
            for (BlueTestResult result : results) {
                if(parent==null) {
                    parent = result.getLink();
                }
                switch (result.getStatus()) {
                    case SKIPPED:
                        skipped++;
                        break;
                    case PASSED:
                        passed++;
                        if ( result.getTestState() == BlueTestResult.State.FIXED )
                        {
                            fixedTotal++;
                        }
                        break;
                    case FAILED:
                        failed++;
                        if ( result.getTestState() == BlueTestResult.State.REGRESSION )
                        {
                            regressions++;
                        }
                        else
                        {
                            existingFailedTotal++;
                        }
                        break;
                }
                total++;
            }
            if (total == 0) {
                return notFound();
            } else {
                BlueTestSummary summary =
                    new BlueTestSummary(passed, failed, fixedTotal, existingFailedTotal, regressions, skipped, total, parent);
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

    public static Result resolve(Run<?,?> run, Reachable parent) {
        Iterable<BlueTestResult> results = new ArrayList<>(0);
        BlueTestSummary summary = new BlueTestSummary(0, 0, 0, 0, 0, 0, 0, //
                                                      parent == null ? null : parent.getLink());
        for (BlueTestResultFactory factory : allFactories()) {
            Result result = factory.getBlueTestResults(run, parent);
            if (result != null && result.results != null && result.summary != null) {
                results = IterableUtils.chainedIterable(result.results, results);
                summary = summary.tally(result.summary);
            }
        }
        return getResult(results, summary);
    }

    private static Result getResult(Iterable<BlueTestResult> results, BlueTestSummary summary) {
        if (summary.getTotal() == 0) {
            summary = null;
            results = null;
        }
        return Result.of(results, summary);
    }

    private static Iterable<BlueTestResultFactory> allFactories() {
        return ExtensionList.lookup(BlueTestResultFactory.class);
    }
}
