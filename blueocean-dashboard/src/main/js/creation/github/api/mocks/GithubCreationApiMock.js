import { Utils } from '@jenkins-cd/blueocean-core-js';
import { ApiMock } from './ApiMock';

import organizations from './organizations';
import repos1 from './repos-1';
import repos2 from './repos-2';
import repos3 from './repos-3';
import creationCreateSuccess from './creation-create-onerepo-success';
import creationUpdateSuccess from './creation-update-onerepo-success';

/* eslint-disable no-unused-vars */

export class GithubCreationApi extends ApiMock {

    listOrganizations(credentialId) {
        const cloned = Utils.clone(organizations);

        for (const org of cloned) {
            if (org.jenkinsOrganizationPipeline) {
                if (this._hasUrlKey('auto-discover=true')) {
                    org.autoDiscover = true;
                } else if (this._hasUrlKey('auto-discover=false')) {
                    org.autoDiscover = false;
                }
            }
        }

        return this._delayedResolve(cloned);
    }

    listRepositories(credentialId, organizationName, pageNumber = 1, pageSize = 100) {
        let repoData = [];

        if (pageNumber === 2) {
            repoData = repos2;
        } else if (pageNumber === 3) {
            repoData = repos3;
        } else {
            repoData = repos1;
        }

        repoData = Utils.clone(repoData);

        for (let index = 0; index < repoData.repositories.items.length; index++) {
            const repo = repoData.repositories.items[index];
            repo.pipelineCreated = false;

            if (this._hasUrlKey('pipeline-created=true') && index % 2 === 0) {
                repo.pipelineCreated = true;
            }
        }

        return this._delayedResolve(repoData);
    }

    createOrgFolder(credentialId, organization, repoNames = []) {
        return this._delayedResolve(creationCreateSuccess);
    }

    updateOrgFolder(credentialId, organization, repoNames = []) {
        return this._delayedResolve(creationUpdateSuccess);
    }

}
