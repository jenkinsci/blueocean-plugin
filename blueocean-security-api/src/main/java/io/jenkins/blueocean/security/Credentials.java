package io.jenkins.blueocean.security;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Predicate;

/**
 * Abstraction to represent credentials of an authentication provider.
 * Must be Jackson serializable
 *
 * @author Vivek Pandey
 */

/**
 * This enables Json polymerphism when mapped to Java
 * At run time, while serializing the listed class inside @JsonSubTypes will have field authProvider
 * field added.
 *
 * While deserializing in to Java, Jackson will look at authProvider field and if it's "github"
 * then will deserialize json in to GithubCredentials.
 *
 * Implementers of Credentials must ensure not to add their own authProvider field.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "authProvider")
@JsonSubTypes({

        })
public interface Credentials{
    /** Predicate used to find matching Credentials by external identity (e.g. Github login or Facebook user id */
    Predicate<Credentials> identityPredicate();
}
