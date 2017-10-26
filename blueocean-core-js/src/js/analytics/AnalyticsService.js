import { Fetch } from '../fetch';
import urlConfig from '../urlconfig';
import utils from '../utils';
import config from '../config';

export class AnalyticsService {
    track(eventName, properties) {
        // Don't scare anyone by posting back stats tracking when usage stats are off
        if (!config.getAnalyticsEnabled()) return false;

        // Go ahead and record usage stats
        const path = urlConfig.getJenkinsRootURL();
        const url = utils.cleanSlashes(`${path}/blue/rest/analytics/track`);
        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: eventName,
                properties: { properties },
            }),
        };
        return Fetch.fetch(url, { fetchOptions });
    }
}
