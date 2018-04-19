// Typdefs for TypedError which can't be TypeScript because we're targeting ES5 and Error's super() messes with this

declare namespace TypedError {

    export interface ServerError {
        code?: number,
        message?: string,
        errors?: Array<any>,
    }

    export class TypedError extends Error {

        constructor(type?: string, serverError?: ServerError);

        populate(type?: string, serverError?: ServerError): TypedError;
    }
}

export = TypedError;
