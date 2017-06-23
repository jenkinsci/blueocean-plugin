import { action, observable } from 'mobx';


class GHEServerManager {

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
        return {
            success: true,
            server,
        };
    }

    _onCreateServerFailure(response) {
        const { errors } = response.responseBody;
        return {
            success: false,
            duplicateName: errors.some(err => err.field === 'name' && err.code === 'ALREADY_EXISTS'),
            duplicateUrl: errors.some(err => err.field === 'apiUrl' && err.code === 'ALREADY_EXISTS'),
        };
    }
}

export default GHEServerManager;
