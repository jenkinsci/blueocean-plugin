/**
 * Created by cmeyers on 9/23/16.
 */

import { Fetch } from '../fetch';
import config from '../urlconfig';
import utils from '../utils';

export class QueueApi {

    fetchQueue(pipeline) {
        const path = config.getJenkinsRootURL();
        const queueUrl = utils.cleanSlashes(`${path}/${pipeline._links.self.href}/queue/`);
        return Fetch.fetchJSON(queueUrl);
    }

    fetchQueueItemFromEvent(queueEvent) {
        const path = config.getJenkinsRootURL();
        const queueItemUrl = utils.cleanSlashes(`${path}/${queueEvent.blueocean_job_rest_url}/queue/${queueEvent.job_run_queueId}`);
        return Fetch.fetchJSON(queueItemUrl);
    }

    removeFromQueue(run) {
        const path = config.getJenkinsRootURL();
        let queueItemUrl;

        // a queue item is a "pseudo run" with the queue href attached via _item
        if (run._queueItem && run._queueItem._links) {
            queueItemUrl = run._queueItem._links.self.href;
        } else {
            console.warn('could not extract data to remove item from queue; aborting');
            return null;
        }

        const removeQueueUrl = utils.cleanSlashes(`${path}/${queueItemUrl}`);

        const fetchOptions = {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return Fetch.fetch(removeQueueUrl, { fetchOptions });
    }

}
