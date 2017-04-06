package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class BlueTestSummary {

    public static final String TOTAL = "total";
    public static final String SKIPPED = "skipped";
    public static final String FAILED = "failed";
    public static final String PASSED = "passed";

    private final long passedTotal;
    private final long failedTotal;
    private final long skippedTotal;
    private final long total;

    public BlueTestSummary(long passedTotal, long failedTotal, long skippedTotal, long total) {
        this.passedTotal = passedTotal;
        this.failedTotal = failedTotal;
        this.skippedTotal = skippedTotal;
        this.total = total;
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

    @Exported(name = TOTAL)
    public long getTotal() {
        return total;
    }
}
