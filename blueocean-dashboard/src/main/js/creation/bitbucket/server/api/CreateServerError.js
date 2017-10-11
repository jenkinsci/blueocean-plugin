import ServerError from './ServerError';

class CreateServerError extends ServerError {
    _handleCustomErrors(errors) {
        this.duplicateName = errors.some(err => err.field === 'name' && err.code === 'ALREADY_EXISTS');
        this.duplicateUrl = errors.some(err => err.field === 'apiUrl' && err.code === 'ALREADY_EXISTS');
        this.invalidUrl = errors.some(err => err.field === 'apiUrl' && err.code === 'INVALID');
    }
}

export default CreateServerError;
