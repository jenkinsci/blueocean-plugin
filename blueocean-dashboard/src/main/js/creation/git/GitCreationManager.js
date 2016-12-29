/**
 * Created by cmeyers on 10/24/16.
 */
import status from './GitCreationStatus';

/**
 * Initiates creation of the pipeline and updates the status as various API calls complete.
 */
export default class GitCreationManager {

    pipeline: null;

    constructor(creationApi, onStatusChanged) {
        this._creationApi = creationApi;
        this._onStatusChanged = onStatusChanged;
    }

    createPipeline(repositoryUrl, credentialId) {
        this._onStatusChanged(status.CREATE_PIPELINE);

        this._creationApi.createPipeline(repositoryUrl, credentialId)
            .then(pipeline => {
                this.pipeline = pipeline;
                this._onStatusChanged(status.COMPLETE);
            });
    }

}
