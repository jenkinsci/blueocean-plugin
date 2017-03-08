package io.jenkins.blueocean.commons;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Describes JSON based error message.
 *
 * {
 *     "code": 401,
 *     "message": "login failed",
 *
 *     "errors" : [ {
 *          "message" : "Invalid password",
 *          "code" : "INVALID",
 *          "field" : "password"
 *     } ]
 * }
 *
 * @author Vivek Pandey
 **/
public class ErrorMessage {

    public final String message;

    public final int code;

    public ErrorMessage(@Nonnull Integer code, @Nonnull String message) {
        this.code=code;
        this.message = message;
    }

    private final List<Error> errors = new ArrayList<>();

    @JsonIgnore
    public ErrorMessage add(Error error){
        errors.add(error);
        return this;
    }

    @JsonIgnore
    public ErrorMessage addAll(Collection<Error> errors){
        this.errors.addAll(errors);
        return this;
    }

    @JsonProperty("errors")
    public List<Error> getErrors(){
        return errors;
    }

    public static class Error{
        //Well known error codes pertaining to field validation
        public enum ErrorCodes  {
            MISSING, ALREADY_EXISTS, INVALID, NOT_FOUND}

        private final String message;
        private final String code;
        private final String field;

        public Error(String field, String code, String message) {
            this.message = message;
            this.code = code;
            this.field = field;
        }

        @JsonProperty("message")
        public String getMessage() {
            return message;
        }

        @JsonProperty("code")
        public String getCode() {
            return code;
        }

        @JsonProperty("field")
        public String getField() {
            return field;
        }
    }
}
