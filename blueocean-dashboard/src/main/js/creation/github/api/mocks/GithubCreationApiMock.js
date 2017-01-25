import { ApiMock } from './ApiMock';

import organizations from './organizations';
import repos1 from './repos-1';
import repos2 from './repos-2';
import repos3 from './repos-3';

export class GithubCreationApi extends ApiMock {

    listOrganizations(credentialId) {
        return this._delayedResolve(organizations);
    }

    listRepositories(credentialId, organizationName, pageNumber = 1, pageSize = 100) {
        let payload = null;

        if (pageNumber === 2) {
            payload = repos2;
        } else if (pageNumber === 3) {
            payload = repos3;
        } else {
            payload = repos1;
        }

        return this._delayedResolve(payload);
    }

    createOrgFolder(credentialId, organization, repoNames = []) {
        // TODO: need response
        return this._delayedResolve(null);
    }

}
