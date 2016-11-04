export default class SSEService {
    constructor(connection) {
        this.connection = connection;
        this._handlers = [];
    }

    _initListeners() {
        console.log('inited');
        if (!this.jobListener) {
            this.jobListener = this.connection.subscribe('job', (event) => {
                this._handleJobEvent(event);
            });
        }
    }

    registerHandler(handlerFn) {
        console.log('registerHandler');
        this._handlers.push(handlerFn);
    }
    _handleJobEvent(event) {
        console.log('handlers', this._handlers);
        this._handlers.forEach(handler => handler(event));
    }
}
