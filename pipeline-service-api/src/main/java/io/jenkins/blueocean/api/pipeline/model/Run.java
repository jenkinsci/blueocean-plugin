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
    /**
     * Name of the organization
     */
    public final String organization;

    /**
     * Run id - unique within a pipeline
     */
    public final String id;

    /**
     * Pipeline name - unique within an organization
     */
    public final String pipeline;

    /**
     * Run status
     */
    public final Status status;

    /**
     * Run start time
     */
    public final Date startTime;

    /**
     * Run end time
     */
    public final Date endTime;

    /**
     * run duration in milli seconds
     */
    public final Long durationInMillis;

    public final String branch;

    public final String commitId;

    public enum Status {
        SUCCESSFUL, FAILING, EXECUTING
    }

    public Run(@Nonnull @JsonProperty("id") String id,
               @Nonnull @JsonProperty("pipeline") String pipeline,
               @Nonnull @JsonProperty("organization") String organization,
               @Nonnull @JsonProperty("status") Status status,
               @Nonnull @JsonProperty("startTime") Date startTime,
               @Nullable @JsonProperty("endTime") Date endTime,
               @Nullable @JsonProperty("durationInMillis") Long durationInMillis,
               @Nullable @JsonProperty("branch") String branch,
               @Nullable @JsonProperty("commitId") String commitId) {
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
    }

    public static class Builder {
        private String organization;
        private String id;
        private String pipeline;
        private Status status;
        private Date startTime;
        private Date endTime;
        private Long durationInMillis;
        private String branch;
        private String commitId;

        public Builder(String id, String pipeline, String organization) {
            this.id = id;
            this.pipeline = pipeline;
            this.organization = pipeline;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder startTime(Date startTime) {
            this.startTime = new Date(startTime.getTime());
            return this;
        }

        public Builder endTime(Date endTime) {
            this.endTime = new Date(endTime.getTime());
            return this;
        }

        public Builder durationInMillis(Long durationInMillis) {
            this.durationInMillis = durationInMillis;
            return this;
        }

        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public Builder commitId(String commitId) {
            this.commitId = commitId;
            return this;
        }

        public Run build() {
            return new Run(id, pipeline, organization, status, startTime, endTime, durationInMillis, branch, commitId);
        }
    }
}
