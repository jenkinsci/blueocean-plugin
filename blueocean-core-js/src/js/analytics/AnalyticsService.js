import { Fetch } from '../fetch';
import config from '../urlconfig';
import utils from '../utils';

export class AnalyticsService {
    track(eventName, properties) {
        const path = config.getJenkinsRootURL();;
        const url = utils.cleanSlashes(`${path}/blue/rest/analytics/track`);
        const body = {
            name: eventName,
            properties: properties
        };
        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: body
        };
        return fetch.fetch(url, { fetchOptions });
    }
}
