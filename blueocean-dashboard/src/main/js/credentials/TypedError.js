/**
 * Utlity class for "typing" of server errors
 */
class TypedError {
    constructor(type, serverError) {
        const { code, message, errors } = serverError || {};
        this.type = type;
        this.code = code;
        this.message = message;
        this.errors = errors;
    }
}

export default TypedError;
