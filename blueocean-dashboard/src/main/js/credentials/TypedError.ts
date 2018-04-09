
type ServerError = any;

/**
 * Utlity class for "typing" of server errors
 */
export class TypedError extends Error {

    type: string;
    code?: string;
    errors?: any;

    constructor(type, serverError?: ServerError) {
        super();
        return this.populate(type, serverError);
    }

    populate(type, serverError?: ServerError) {
        const {
            code = undefined,
            message = undefined,
            errors = undefined
        } = serverError || {};
        this.type = type;
        this.code = code;
        this.message = message;
        this.errors = errors;

        return this;
    }
}

