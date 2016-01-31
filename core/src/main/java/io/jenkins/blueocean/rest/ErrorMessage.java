package io.jenkins.blueocean.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Describes JSON based error message.
 *
 * {
 *     "message": "Password must be 8 chars",
 *
 *     "errors":[{
 *         "field":"password",
 *     }]
 * }
 *
 * @author Vivek Pandey
 **/
public class ErrorMessage {

    public final String message;

    public ErrorMessage(String message) {
        this.message = message;
    }

    @JsonProperty("errors")
    private final List<Map<String,String>> errors = new ArrayList<Map<String, String>>();

    public ErrorMessage add(Map<String, String> error){
        errors.add(error);
        return this;
    }
}
