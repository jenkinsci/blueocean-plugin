import ServerError from './ServerError';


class CreateServerError extends ServerError {

    _handleCustomErrors(errors) {
        this.duplicateName = errors.some(err => err.field === 'name' && err.code === 'ALREADY_EXISTS');
        this.duplicateUrl = errors.some(err => err.field === 'apiUrl' && err.code === 'ALREADY_EXISTS');
        this.invalidServer = errors.some(err => err.field === 'apiUrl' && err.code === 'INVALID' && err.message.indexOf('check hostname') !== -1);
        this.invalidApiUrl = errors.some(err => err.field === 'apiUrl' && err.code === 'INVALID' && err.message.indexOf('check path') !== -1);
    }

}

export default CreateServerError;
