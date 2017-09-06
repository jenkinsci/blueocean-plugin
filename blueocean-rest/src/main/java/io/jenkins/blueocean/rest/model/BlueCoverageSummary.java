package io.jenkins.blueocean.rest.model;

import org.kohsuke.stapler.export.Exported;

/**
 * @author cliffmeyers
 */
public final class BlueCoverageSummary {

    public static final String CLASSES = "classes";
    public static final String METHODS = "methods";
    public static final String LINES = "lines";
    public static final String BRANCHES = "branches";
    public static final String INSTRUCTIONS = "instructions";

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

    @Exported(name = CLASSES)
    public BlueCoverageMetrics getClasses() {
        return classes;
    }

    @Exported(name = METHODS)
    public BlueCoverageMetrics getMethods() {
        return methods;
    }

    @Exported(name = LINES)
    public BlueCoverageMetrics getLines() {
        return lines;
    }

    @Exported(name = BRANCHES)
    public BlueCoverageMetrics getBranches() {
        return branches;
    }

    @Exported(name = INSTRUCTIONS)
    public BlueCoverageMetrics getInstructions() {
        return instructions;
    }
}
