package io.jenkins.blueocean.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * @author Vivek Pandey
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({METHOD})
public @interface Navigable {
}
