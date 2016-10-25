/**
 * Created by cmeyers on 10/24/16.
 */
import status from './GitCreationStatus';

/**
 * Initiates creation of the pipeline and updates the status as various API calls complete.
 */
export default class GitCreationManager {

    constructor(creationApi, onStatusChanged) {
        this._creationApi = creationApi;
        this._onStatusChanged = onStatusChanged;
    }

    createWithSshKeyCredential(repositoryUrl, sshKey) {
        this._onStatusChanged(status.CREATE_CREDS);

        return this._creationApi.saveSshKeyCredential(sshKey)
            .then(credentialId => (
                this._createPipeline(repositoryUrl, credentialId)
            )
        );
    }

    // eslint-disable-next-line no-unused-vars
    createWithUsernamePasswordCredential(repositoryUrl, username, password) {
        return this.createWithSshKeyCredential();
    }

    // eslint-disable-next-line no-unused-vars
    createWithSystemSshCredential(repositoryUrl) {
        return this.createWithSshKeyCredential();
    }

    _createPipeline(repositoryUrl, credentialId) {
        this._onStatusChanged(status.CREATE_PIPELINE);

        return this._creationApi.createPipeline(repositoryUrl, credentialId)
            .then(() => {
                this._onStatusChanged(status.RUN_PIPELINE);
                setTimeout(() => {
                    this._onStatusChanged(status.COMPLETE);
                }, 2000);
            });
    }

}
