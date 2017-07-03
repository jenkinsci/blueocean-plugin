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
