/**
 * Utlity class for "typing" of server errors
 *
 * NB: Typescript Can't extend Error when targetting ES5 because invoking Error() as a super() creates a new this.
 *
 * Typedefs are found in TypedError.d.ts
 */

export function TypedError(type, serverError) {
    const self = new Error();
    self.populate = populate.bind(self);
    self.__TypedError = true;
    return self.populate(type, serverError);

    function populate(type, serverError) {
        const { code, message, errors } = serverError || {};
        this.type = type;
        this.code = code;
        this.message = message || type;
        this.errors = errors;

        return this;
    }
}
