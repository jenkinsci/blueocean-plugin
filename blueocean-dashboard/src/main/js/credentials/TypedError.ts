/**
 * Utlity class for "typing" of server errors
 */

export interface ServerError {
    code?: number;
    message?: string;
    errors?: Array<string>;
}

export class TypedError extends Error {
    type: string;
    code: number;
    errors: Array<string>;

    constructor(type?: string, serverError?: ServerError) {
        super();

        // When subclassing Error and targetting ES5, super() wrecks our prototype chain
        this.constructor = TypedError;
        if ((Object as any).setPrototypeOf) {
            (Object as any).setPrototypeOf(this, TypedError.prototype);
        } else {
            (this as any).__proto__ = TypedError.prototype;
        }

        this.name = 'TypedError';
        return this.populate(type || 'TypedError', serverError);
    }

    populate(type: string, serverError?: ServerError) {
        const { code = -1, message = undefined, errors = [] } = serverError || {};

        this.type = type;
        this.code = code;
        this.message = message || String(serverError || type);
        this.errors = errors;

        return this;
    }
}
