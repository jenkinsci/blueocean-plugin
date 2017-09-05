package io.jenkins.blueocean.service.embedded.rest.coverage;

/**
 * @author cliffmeyers
 */
public final class BlueCoverageSummary {

    private final BlueCoverageMetrics classes;
    private final BlueCoverageMetrics methods;
    private final BlueCoverageMetrics lines;
    private final BlueCoverageMetrics branches;
    private final BlueCoverageMetrics instructions;

    public BlueCoverageSummary(BlueCoverageMetrics classes, BlueCoverageMetrics methods, BlueCoverageMetrics lines, BlueCoverageMetrics branches, BlueCoverageMetrics instructions) {
        this.classes = classes;
        this.methods = methods;
        this.lines = lines;
        this.branches = branches;
        this.instructions = instructions;
    }

    public BlueCoverageSummary tally(BlueCoverageSummary other) {
        return new BlueCoverageSummary(
            classes.tally(other.getClasses()),
            methods.tally(other.getMethods()),
            lines.tally(other.getLines()),
            branches.tally(other.getBranches()),
            instructions.tally(other.getInstructions())
        );
    }

    public BlueCoverageMetrics getClasses() {
        return classes;
    }

    public BlueCoverageMetrics getMethods() {
        return methods;
    }

    public BlueCoverageMetrics getLines() {
        return lines;
    }

    public BlueCoverageMetrics getBranches() {
        return branches;
    }

    public BlueCoverageMetrics getInstructions() {
        return instructions;
    }
}
