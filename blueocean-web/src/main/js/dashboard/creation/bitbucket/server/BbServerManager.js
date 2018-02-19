import { action, observable } from 'mobx';

import CreateServerError from './api/CreateServerError';

class BbServerManager {

    @observable
    servers = [];

    constructor(serverApi) {
        this.serverApi = serverApi;
    }

    listServers() {
        return this.serverApi.listServers()
            .then(servers => this._onListServersSuccess(servers));
    }

    @action
    _onListServersSuccess(servers) {
        this.servers.replace(servers);
        return servers;
    }

    validateVersion(id) {
        return this.serverApi.validateVersion(id)
            .then(
                success => this._onValidateVersionSuccess(success),
                error => this._onValidateVersionFailure(error),
            );
    }

    _onValidateVersionSuccess(success) {
        return success;
    }

    _onValidateVersionFailure(error) {
        throw new CreateServerError(error);
    }

    createServer(name, url) {
        return this.serverApi.createServer(name, url)
            .then(
                server => this._onCreateServerSuccess(server),
                error => this._onCreateServerFailure(error),
            );
    }

    @action
    _onCreateServerSuccess(server) {
        this.servers.push(server);
        return server;
    }

    _onCreateServerFailure(response) {
        throw new CreateServerError(response);
    }
}

export default BbServerManager;
