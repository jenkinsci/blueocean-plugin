

class ServerError {

    constructor(response) {
        const { code, message, errors } = response.responseBody;

        this.code = code;
        this.message = message;
        this.errors = errors;

        this._handleCustomErrors(errors, code, message);
    }

    // eslint-disable-next-line no-unused-vars
    _handleCustomErrors(errors, code, message) {}

}

export default ServerError;
