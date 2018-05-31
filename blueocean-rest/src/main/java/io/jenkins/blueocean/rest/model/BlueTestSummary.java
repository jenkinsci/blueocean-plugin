package io.jenkins.blueocean.rest.model;

import io.jenkins.blueocean.rest.hal.Link;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public final class BlueTestSummary extends Resource {

    public static final String TOTAL = "total";
    public static final String SKIPPED = "skipped";
    public static final String FAILED = "failed";
    public static final String PASSED = "passed";
    public static final String FIXED = "fixed";
    public static final String EXISTING_FAILED = "existingFailed";
    public static final String REGRESSIONS = "regressions";

    private final long passedTotal;
    private final long failedTotal;
    private final long fixedTotal;
    private final long existingFailedTotal;
    private final long regressionsTotal;
    private final long skippedTotal;
    private final long total;

    private final Link parent;
    private Link selfLink;

    public BlueTestSummary(long passedTotal, long failedTotal, long fixedTotal, long existingFailedTotal,
                           long regressionsTotal, long skippedTotal, long total, Link parent) {
        this.passedTotal = passedTotal;
        this.failedTotal = failedTotal;
        this.fixedTotal = fixedTotal;
        this.existingFailedTotal = existingFailedTotal;
        this.regressionsTotal = regressionsTotal;
        this.skippedTotal = skippedTotal;
        this.total = total;
        this.parent = parent;
    }

    @Exported(name = PASSED)
    public long getPassedTotal() {
        return passedTotal;
    }

    @Exported(name = FAILED)
    public long getFailedTotal() {
        return failedTotal;
    }

    @Exported(name = SKIPPED)
    public long getSkippedTotal() {
        return skippedTotal;
    }

    @Exported(name = FIXED)
    public long getFixedTotal() {
        return fixedTotal;
    }

    @Exported(name = EXISTING_FAILED)
    public long getExistingFailedTotal() {
        return existingFailedTotal;
    }

    @Exported(name = REGRESSIONS)
    public long getRegressionsTotal() {
        return regressionsTotal;
    }

    @Exported(name = TOTAL)
    public long getTotal() {
        return total;
    }

    public BlueTestSummary tally(BlueTestSummary summary) {
        return new BlueTestSummary(
            this.passedTotal + summary.passedTotal,
            this.failedTotal + summary.failedTotal,
            this.fixedTotal + summary.fixedTotal,
            this.existingFailedTotal + summary.existingFailedTotal,
            this.regressionsTotal + summary.regressionsTotal,
            this.skippedTotal + summary.skippedTotal,
            this.total + summary.total,
             summary.parent
        );
    }

    @Override
    public Link getLink()
    {
        return this.selfLink == null ? parent.rel( "/blueTestSummary" ) : this.selfLink;
    }

    public void setLink(Link link)
    {
        this.selfLink = link;
    }
}
