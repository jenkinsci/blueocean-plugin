/**
 * Created by cmeyers on 8/29/16.
 */
import { UrlConfig } from '../urlconfig';
import { Fetch } from '../fetch';
import { Utils } from '../utils';

export class RunApi {
    startRun(item) {
        const path = UrlConfig.getJenkinsRootURL();
        const runUrl = Utils.cleanSlashes(`${path}/${item._links.self.href}/runs/`);

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return Fetch.fetchJSON(runUrl, { fetchOptions });
    }

    stopRun(run) {
        const path = UrlConfig.getJenkinsRootURL();
        const runUrl = run._links.self.href;
        const stopUrl = Utils.cleanSlashes(`${path}/${runUrl}/stop/?blocking=true&timeOutInSecs=10`);

        const fetchOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return Fetch.fetch(stopUrl, { fetchOptions });
    }

    replayRun(run) {
        const path = UrlConfig.getJenkinsRootURL();
        const runUrl = run._links.self.href;
        const replayPipelineUrl = Utils.cleanSlashes(`${path}/${runUrl}/replay/`);

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return Fetch.fetchJSON(replayPipelineUrl, { fetchOptions });
    }

    restartStage(run, nodeId) {
        const path = UrlConfig.getJenkinsRootURL();
        const runUrl = run._links.self.href;
        const restartStageUrl = Utils.cleanSlashes(`${path}/${runUrl}/nodes/${nodeId}/restart`);

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                restart: 'true',
            }),
        };

        return Fetch.fetchJSON(restartStageUrl, { fetchOptions });
    }
}
