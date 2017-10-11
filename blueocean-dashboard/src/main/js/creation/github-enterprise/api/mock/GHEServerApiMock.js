import { ApiMock } from '../../../github/api/mocks/ApiMock';

/**
 * Handles lookup of Github orgs and repos, and saving of the Github org folder.
 */
class GHEServerApi extends ApiMock {
    listServers({ hasServer = true } = {}) {
        const servers = [];

        if (hasServer) {
            servers.push({
                name: 'StavroCorp',
                apiUrl: 'https://github.stavrocorp.com',
            });
        }

        return this._delayedResolve(servers);
    }

    createServer(serverName = '', serverUrl = '') {
        const errors = [];

        if (serverName.toLowerCase() === 'duplicate') {
            errors.push({ field: 'name', code: 'ALREADY_EXISTS' });
        }
        if (serverUrl.toLowerCase() === 'duplicate') {
            errors.push({ field: 'apiUrl', code: 'ALREADY_EXISTS' });
        }

        if (errors.length) {
            return this._delayedReject({
                code: 400,
                errors,
            });
        }

        return this._delayedResolve({
            name: serverName,
            apiUrl: serverUrl,
        });
    }
}

export default GHEServerApi;
