/**
 * Created by cmeyers on 8/29/16.
 */
import { Fetch } from '../fetch';
import config from '../urlconfig';

import { cleanSlashes } from '../sse/UrlUtils';

export class RunApi {

    startRun(item) {
        const path = config.getJenkinsRootURL();
        const runUrl = cleanSlashes(`${path}/${item._links.self.href}/runs/`);

        const fetchOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return Fetch.fetch(runUrl, { fetchOptions });
    }

    stopRun(run) {
        const path = config.getJenkinsRootURL();
        const runUrl = run._links.self.href;
        const stopUrl = cleanSlashes(`${path}/${runUrl}/stop/?blocking=true&timeOutInSecs=10`);

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
        const replayPipelineUrl = cleanSlashes(`${path}/${runUrl}/replay/`);

        const fetchOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return Fetch.fetchJSON(replayPipelineUrl, { fetchOptions });
    }

}
