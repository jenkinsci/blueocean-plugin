import Utils from '../utils';

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

    /**
     * Add a handler for job events.
     *
     * @param {Function} handlerFn
     * @returns {string} id used by removeHandler
     */
    registerHandler(handlerFn) {
        const id = Utils.randomId('sse');

        this._handlers.push({
            id,
            handlerFn,
        });

        return id;
    }

    /**
     * Remove a previously-registered handler.
     * @param id
     */
    removeHandler(id) {
        this._handlers = this._handlers.filter(handler => handler.id !== id);
    }

    _handleJobEvent(event) {
        this._handlers.forEach(handler => handler.handlerFn(event));
    }
}
