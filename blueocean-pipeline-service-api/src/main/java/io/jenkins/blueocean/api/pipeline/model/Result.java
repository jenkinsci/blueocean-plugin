package io.jenkins.blueocean.api.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Marker interface that describes build result
 *
 * @author Vivek Pandey
 * @see PipelineResult
 * @see JobResult
 * @see Run
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "resultType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JobResult.class, name = "JOB"),
        @JsonSubTypes.Type(value = PipelineResult.class, name = "PIPELINE") })
public interface Result {

    /** Result types, must be all names defined inside JsonSubTypes annotation */
    enum Type {JOB, PIPELINE};

    /**
     * Gives what kind of Result this implements. This is to help a json processor client
     * to know what kind of Result it is from the JSON packet of {@link Run}
     *
     * We don't serialize it in json as Jackson already adds resultType object.
     *
     * @return One of {@link Type}
     */
    @JsonIgnore
    Type getType();
}
