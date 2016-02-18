package io.jenkins.blueocean.commons;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This exception class to be used by all service methods.
 *
 * An error is represented by a code, in case of HTTP transport it could be http specific error code.
 *
 * Error message is represented by {@link ErrorMessage}
 *
 * @author Vivek Pandey
 */
public class ServiceException extends RuntimeException{
    public final int status;
    public final ErrorMessage errorMessage;

    public ServiceException(int status, String message) {
        super(message);
        this.status = status;
        this.errorMessage = new ErrorMessage(status, message);
    }

    public ServiceException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorMessage = new ErrorMessage(status,message);
    }

    public ServiceException(int status, ErrorMessage errorMessage, Throwable cause) {
        super(errorMessage.message, cause);
        this.status = status;
        this.errorMessage = errorMessage;
    }

    /** Convert ErrorMessage to JSON */
    public String toJson(){
        return JsonConverter.toJson(errorMessage);
    }

    /**
     * Describes JSON based error message.
     *
     * {
     *     "code": 401,
     *     "message": "Password must be 8 chars",
     *
     *     "errors":[{
     *         "field":"password",
     *     }]
     * }
     *
     * @author Vivek Pandey
     **/
    public static class ErrorMessage {

        public final String message;

        public final int code;

        public ErrorMessage(@NonNull Integer code, @NonNull String message) {
            this.code=code;
            this.message = message;
        }

        @JsonProperty("errors")
        private final List<Map<String,String>> errors = new ArrayList<Map<String, String>>();

        @JsonIgnore
        public ErrorMessage add(Map<String, String> error){
            errors.add(ImmutableMap.copyOf(error));
            return this;
        }

        @JsonIgnore
        public Iterator<Map<String,String>> getErrors(){
            return new Iterator<Map<String, String>>() {
                @Override
                public boolean hasNext() {
                    return errors.iterator().hasNext();
                }

                @Override
                public Map<String, String> next() {
                    return errors.iterator().next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    /** Convenience exception classes modeled after HTTP exceptions */
    public static class NotFoundException extends ServiceException{

        public NotFoundException(String message) {
            super(NOT_FOUND, message);
        }

        public NotFoundException(String message, Throwable throwable ) {
            super(NOT_FOUND, message, throwable);
        }

        public NotFoundException(ErrorMessage errorMessage) {
            super(NOT_FOUND, errorMessage.message);
        }
        public NotFoundException(ErrorMessage errorMessage, Throwable throwable ) {
            super(NOT_FOUND, errorMessage.message, throwable);
        }
    }

    public static class ForbiddenException extends ServiceException{

        public ForbiddenException(String message) {
            super(FORBIDDEN, message);
        }

        public ForbiddenException(String message, Throwable throwable ) {
            super(FORBIDDEN, message, throwable);
        }

        public ForbiddenException(ErrorMessage errorMessage) {
            super(FORBIDDEN, errorMessage.message);
        }
        public ForbiddenException(ErrorMessage errorMessage, Throwable throwable ) {
            super(FORBIDDEN, errorMessage.message, throwable);
        }
    }

    public static class UnauthorizedException extends ServiceException{

        public UnauthorizedException(String message) {
            super(UNAUTHORIZED, message);
        }

        public UnauthorizedException(String message, Throwable throwable ) {
            super(UNAUTHORIZED, message, throwable);
        }

        public UnauthorizedException(ErrorMessage errorMessage) {
            super(UNAUTHORIZED, errorMessage.message);
        }
        public UnauthorizedException(ErrorMessage errorMessage, Throwable throwable ) {
            super(UNAUTHORIZED, errorMessage.message, throwable);
        }
    }

    public static class BadRequestExpception extends ServiceException{

        public BadRequestExpception(String message) {
            super(BAD_REQUEST, message);
        }

        public BadRequestExpception(String message, Throwable throwable ) {
            super(BAD_REQUEST, message, throwable);
        }

        public BadRequestExpception(ErrorMessage errorMessage) {
            super(BAD_REQUEST, errorMessage.message);
        }
        public BadRequestExpception(ErrorMessage errorMessage, Throwable throwable ) {
            super(BAD_REQUEST, errorMessage.message, throwable);
        }
    }

    public static class UnprocessableEntityException extends ServiceException{

        public UnprocessableEntityException(String message) {
            super(UNPROCESSABLE_ENTITY, message);
        }

        public UnprocessableEntityException(String message, Throwable throwable ) {
            super(UNPROCESSABLE_ENTITY, message, throwable);
        }

        public UnprocessableEntityException(ErrorMessage errorMessage) {
            super(UNPROCESSABLE_ENTITY, errorMessage.message);
        }
        public UnprocessableEntityException(ErrorMessage errorMessage, Throwable throwable ) {
            super(UNPROCESSABLE_ENTITY, errorMessage.message, throwable);
        }
    }

    public static class ConflictException extends ServiceException{

        public ConflictException(String message) {
            super(CONFLICT, message);
        }

        public ConflictException(String message, Throwable throwable ) {
            super(CONFLICT, message, throwable);
        }

        public ConflictException(ErrorMessage errorMessage) {
            super(CONFLICT, errorMessage.message);
        }
        public ConflictException(ErrorMessage errorMessage, Throwable throwable ) {
            super(CONFLICT, errorMessage.message, throwable);
        }
    }

    public static class TooManyRequestsException extends ServiceException{

        public TooManyRequestsException(String message) {
            super(TOO_MANY_REQUESTS, message);
        }

        public TooManyRequestsException(String message, Throwable throwable ) {
            super(TOO_MANY_REQUESTS, message, throwable);
        }

        public TooManyRequestsException(ErrorMessage errorMessage) {
            super(TOO_MANY_REQUESTS, errorMessage.message);
        }
        public TooManyRequestsException(ErrorMessage errorMessage, Throwable throwable ) {
            super(TOO_MANY_REQUESTS, errorMessage.message, throwable);
        }
    }

    public static class UnexpectedErrorExpcetion extends ServiceException{

        public UnexpectedErrorExpcetion(String message) {
            super(INTERNAL_SERVER_ERROR, message);
        }

        public UnexpectedErrorExpcetion(String message, Throwable throwable ) {
            super(INTERNAL_SERVER_ERROR, message, throwable);
        }

        public UnexpectedErrorExpcetion(ErrorMessage errorMessage) {
            super(INTERNAL_SERVER_ERROR, errorMessage.message);
        }
        public UnexpectedErrorExpcetion(ErrorMessage errorMessage, Throwable throwable ) {
            super(INTERNAL_SERVER_ERROR, errorMessage.message, throwable);
        }
    }

    public static class NotImplementedException extends ServiceException{

        public NotImplementedException(String message) {
            super(NOT_IMPLEMENTED, message);
        }

        public NotImplementedException(String message, Throwable throwable ) {
            super(NOT_IMPLEMENTED, message, throwable);
        }

        public NotImplementedException(ErrorMessage errorMessage) {
            super(NOT_IMPLEMENTED, errorMessage.message);
        }
        public NotImplementedException(ErrorMessage errorMessage, Throwable throwable ) {
            super(NOT_IMPLEMENTED, errorMessage.message, throwable);
        }
    }

    public static final int BAD_REQUEST = 401;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int CONFLICT = 409;
    public static final int UNPROCESSABLE_ENTITY = 422;
    public static final int TOO_MANY_REQUESTS = 429;
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
}
