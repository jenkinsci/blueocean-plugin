package io.jenkins.blueocean.rest.model;

/**
 * @author cliffmeyers
 */
public final class BlueCoverageMetrics {

    private final long covered;
    private final long missed;
    private final int percent;

    public BlueCoverageMetrics(long covered, long missed) {
        this.covered = covered;
        this.missed = missed;
        this.percent = (int) ((double) covered / (covered + missed) * 100);
    }

    public long getCovered() {
        return covered;
    }

    public long getMissed() {
        return missed;
    }

    public BlueCoverageMetrics tally(BlueCoverageMetrics other) {
        return new BlueCoverageMetrics(covered + other.getCovered(), missed + other.getMissed());
    }

    public int getPercent() {
        return percent;
    }

}
