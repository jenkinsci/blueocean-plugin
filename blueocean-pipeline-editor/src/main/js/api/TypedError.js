/**
 * Utlity class for "typing" of server errors
 */
export class TypedError {
    constructor(type, serverError) {
        this.type = type;

        if (serverError) {
            this.code = serverError.code;
            this.message = serverError.message;
            this.errors = serverError.errors;
        }
    }
}
