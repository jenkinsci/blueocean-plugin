import { Utils } from '@jenkins-cd/blueocean-core-js';
import { ApiMock } from './ApiMock';

import creationCreateSuccess from './creation-create-onerepo-success';
import creationUpdateSuccess from './creation-update-onerepo-success';
import organizations from './organizations';
import orgfolderSuccess from './orgfolder-success';
import repos1 from './repos-1';
import repos2 from './repos-2';
import repos3 from './repos-3';

/* eslint-disable no-unused-vars */

export class GithubCreationApi extends ApiMock {

    listOrganizations(credentialId) {
        const cloned = Utils.clone(organizations);
        return this._delayedResolve(cloned);
    }

    listRepositories(credentialId, organizationName, pageNumber = 1, pageSize = 100) {
        let repoData = null;

        if (pageNumber === 2) {
            repoData = repos2;
        } else if (pageNumber === 3) {
            repoData = repos3;
        } else {
            repoData = repos1;
        }

        repoData = Utils.clone(repoData);

        if (this._hasUrlKey('no-repos=true')) {
            repoData.repositories.items = [];
            repoData.repositories.nextPage = null;
        } else if (this._hasUrlKey('no-selectable-repos=true')) {
            repoData.repositories.items = repoData.repositories.items.slice(0, 2);
            repoData.repositories.nextPage = null;
        }

        return this._delayedResolve(repoData);
    }

    findExistingOrgFolder(organization) {
        if (this._hasUrlKey('orgfolder-exists=true')) {
            const orgFolder = Utils.clone(orgfolderSuccess);

            if (this._hasUrlKey('single-repo=true')) {
                orgFolder.requestedRepos = orgFolder.pipelines;
            }

            return this._delayedResolve({
                isFound: true,
                isOrgFolder: true,
                orgFolder,
            });
        } else if (this._hasUrlKey('orgfolder-invalid=true')) {
            return this._delayedResolve({
                isFound: true,
                isOrgFolder: false,
            });
        }

        // by default, the org folder doesn't exist
        // this puts the flow into 'create' mode
        return this._delayedResolve({
            isFound: false,
            isOrgFolder: false,
        });
    }


    createOrgFolder(credentialId, organization, repoNames = []) {
        console.log('createOrgFolder called with', ...arguments);
        return this._delayedResolve(creationCreateSuccess);
    }

    updateOrgFolder(credentialId, orgFolder, repoNames = []) {
        console.log('updateOrgFolder called with', ...arguments);
        return this._delayedResolve(creationUpdateSuccess);
    }

}
