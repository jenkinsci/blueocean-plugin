export class SSEService {
    constructor(connection) {
        this.connection = connection;
        this._handlers = [];
    }

    _initListeners() {
        if (!this.jobListener) {
            this.jobListener = this.connection.subscribe('job', (event) => {
                this._handleJobEvent(event);
            });
        }
    }

    registerHandler(handlerFn) {
        this._handlers.push(handlerFn);
    }
    _handleJobEvent(event) {
        this._handlers.forEach(handler => handler(event));
    }
}
