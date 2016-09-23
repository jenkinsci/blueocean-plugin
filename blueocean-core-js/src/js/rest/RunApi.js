/**
 * Created by cmeyers on 8/29/16.
 */
import { Fetch } from '../fetch';
import config from '../urlconfig';
import utils from '../utils';

export class RunApi {

    fetchRunFromEvent(runEvent, useExpectedBuildNumber) {
        const baseUrl = config.getJenkinsRootURL();
        const buildNumber = useExpectedBuildNumber ? runEvent.blueocean_job_expected_build_number : runEvent.jenkins_object_id;
        const runUrl = utils.cleanSlashes(`${baseUrl}/${runEvent.blueocean_job_rest_url}/runs/${buildNumber}`);
        return Fetch.fetchJSON(runUrl);
    }

    startRun(item) {
        const path = config.getJenkinsRootURL();
        const runUrl = utils.cleanSlashes(`${path}/${item._links.self.href}/runs/`);

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return Fetch.fetchJSON(runUrl, { fetchOptions });
    }

    stopRun(run) {
        const path = config.getJenkinsRootURL();
        const runUrl = run._links.self.href;
        const stopUrl = utils.cleanSlashes(`${path}/${runUrl}/stop/?blocking=true&timeOutInSecs=10`);

        const fetchOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return Fetch.fetch(stopUrl, { fetchOptions });
    }

    replayRun(run) {
        const path = config.getJenkinsRootURL();
        const runUrl = run._links.self.href;
        const replayPipelineUrl = utils.cleanSlashes(`${path}/${runUrl}/replay/`);

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return Fetch.fetchJSON(replayPipelineUrl, { fetchOptions });
    }

}
