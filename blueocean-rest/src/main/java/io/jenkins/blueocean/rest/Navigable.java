package io.jenkins.blueocean.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Methods annotated as Navigable appear as link in the enclosing class'es _links object.
 *
 * <pre>
 *     <code>
 *
 *         &#064;Navigable
 *         Exported(name="pipelines")
 *         public BluePipelineContainer getPipelines();
 *
 *         Results in to:
 *
 *         "_links":{
 *             "pipelines":{"href":"/pipelines"}
 *         }
 *     </code>
 * </pre>
 *
 * @author Vivek Pandey
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({METHOD})
public @interface Navigable {
}
