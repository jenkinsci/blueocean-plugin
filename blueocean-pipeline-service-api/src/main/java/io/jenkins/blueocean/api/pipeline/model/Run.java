package io.jenkins.blueocean.api.pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;

/**
 * Run is abstraction of pipeline run.
 *
 * @author Vivek Pandey
 */
public final class Run {
    /** Name of the organization */
    @JsonProperty("organization")
    public final String organization;

    /** Run id - unique within a pipeline */
    @JsonProperty("id")
    public final String id;

    /** Pipeline name - unique within an organization */
    @JsonProperty("pipeline")
    public final String pipeline;

    /** Run status */
    @JsonProperty("status")
    public final Status status;

    /** Run trend */
    @JsonProperty("runTrend")
    public final RunTrend runTrend;


    /** Build execution start time inside executor */
    @JsonProperty("startTime")
    public final Date startTime;

    /** Time when build is scheduled and is in queue waiting for executor */
    @JsonProperty("enQueueTime")
    public final Date enQueueTime;

    /**Build end time*/
    @JsonProperty("endTime")
    public final Date endTime;

    /**  Build duration in milli seconds */
    @JsonProperty("durationInMillis")
    public final Long durationInMillis;

    /**  Branch on which build is executed */
    @JsonProperty("branch")
    public final String branch;

    /** Commit id on which build is executing */
    @JsonProperty("commitId")
    public final String commitId;

    /** Build summary */
    @JsonProperty("runSummary")
    public final String runSummary;

    /** Result of run */
    @JsonProperty("result")
    public final Result result;

    public enum Status {
        /** Build completed successfully */
        SUCCESSFUL,

        /** Build failed */
        FAILING,

        /** Build is executing, not in the queue */
        EXECUTING,

        /** Build is in queue, waiting to be executed */
        IN_QUEUE,

        /** Build was aborted by user */
        ABORTED,

        /** Unknown status */
        UNKNOWN,

        /** In multi stage build (maven2), a build step might not execute due to failure in previous step */
        NOT_BUILT
    }

    public enum RunTrend {
        /** Build was broekn in earlier build, got fixed now */
        FIXED,

        /** Build has been broken for a while */
        BROKEN_FOR_LONG_TIME
    }

    public Run(@Nonnull @JsonProperty("id") String id,
               @Nonnull @JsonProperty("pipeline") String pipeline,
               @Nonnull @JsonProperty("organization") String organization,
               @Nonnull @JsonProperty("status") Status status,
               @Nonnull @JsonProperty("runTrend") RunTrend runTrend,
               @Nonnull @JsonProperty("startTime") Date startTime,
               @Nonnull @JsonProperty("enQueueTime") Date enQueueTime,
               @Nullable @JsonProperty("endTime") Date endTime,
               @Nullable @JsonProperty("durationInMillis") Long durationInMillis,
               @Nullable @JsonProperty("branch") String branch,
               @Nullable @JsonProperty("commitId") String commitId,
               @Nullable @JsonProperty("runSummary") String runSummary,
               @Nullable @JsonProperty("result") Result result) {
        this.organization = organization;
        this.id = id;
        this.pipeline = pipeline;
        this.status = status;
        this.startTime = new Date(startTime.getTime());
        if(endTime != null) {
            this.endTime = new Date(endTime.getTime());
        }else{
            this.endTime = null;
        }
        this.durationInMillis = durationInMillis;
        this.branch = branch;
        this.commitId = commitId;
        this.runSummary = runSummary;
        this.runTrend = runTrend;
        this.enQueueTime = new Date(enQueueTime.getTime());
        this.result = result;
//        if(result != null) {
//            this.resultType = result.getType();
//        }else{
//            this.resultType = null;
//        }

    }

    public static class Builder {
        private String organization;
        private String id;
        private String pipeline;
        private Status status;
        private Date startTime;
        private Date enQueueTime;
        private Date endTime;
        private Long durationInMillis;
        private String branch;
        private String commitId;
        private String runSummary;
        private RunTrend runTrend;
        private Result result;

        public Builder(@Nonnull String id, @Nonnull String pipeline, @Nonnull String organization) {
            this.id = id;
            this.pipeline = pipeline;
            this.organization = organization;
        }

        public Builder status(@Nonnull Status status) {
            this.status = status;
            return this;
        }

        public Builder startTime(@Nonnull Date startTime) {
            this.startTime = new Date(startTime.getTime());
            return this;
        }

        public Builder enQueueTime(@Nonnull Date enQueueTime) {
            this.enQueueTime = new Date(enQueueTime.getTime());
            return this;
        }

        public Builder endTime(@Nullable  Date endTime) {
            this.endTime = endTime != null ? new Date(endTime.getTime()) : null;
            return this;
        }

        public Builder durationInMillis(@Nullable Long durationInMillis) {
            this.durationInMillis = durationInMillis;
            return this;
        }

        public Builder branch(@Nullable String branch) {
            this.branch = branch;
            return this;
        }

        public Builder commitId(@Nullable String commitId) {
            this.commitId = commitId;
            return this;
        }

        public Builder runSummary(String runSummary){
            this.runSummary = runSummary;
            return this;
        }

        public Builder runTrend(RunTrend runTrend){
            this.runTrend = runTrend;
            return this;
        }

        public Builder result(Result result){
            this.result = result;
            return this;
        }

        public Run build() {
            return new Run(id, pipeline, organization, status, runTrend, startTime, enQueueTime, endTime,
                    durationInMillis, branch, commitId, runSummary, result);
        }
    }
}
