import { Fetch } from '../fetch';
import { UrlConfig } from '../urlconfig';
import { Utils } from '../utils';
import { AppConfig } from '../config';

export class AnalyticsService {
    track(eventName, properties) {
        // Don't scare anyone by posting back stats tracking when usage stats are off
        if (!AppConfig.getAnalyticsEnabled()) return false;

        // Go ahead and record usage stats
        const path = UrlConfig.getJenkinsRootURL();
        const url = Utils.cleanSlashes(`${path}/blue/rest/analytics/track`);
        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: eventName,
                properties: properties,
            }),
        };
        return Fetch.fetch(url, { fetchOptions });
    }
}
