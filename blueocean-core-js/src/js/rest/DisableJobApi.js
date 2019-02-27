import { UrlConfig } from '../urlconfig';
import { Fetch } from '../fetch';
import { Utils } from '../utils';

export class DisableJobApi {
    disable(item) {
        const path = UrlConfig.getJenkinsRootURL();
        const runUrl = Utils.cleanSlashes(`${path}/${item._links.self.href}/disable/`);

        const fetchOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return Fetch.fetchJSON(runUrl, { fetchOptions });
    }

    enable(item) {
        const path = UrlConfig.getJenkinsRootURL();
        const runUrl = Utils.cleanSlashes(`${path}/${item._links.self.href}/enable/`);

        const fetchOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return Fetch.fetchJSON(runUrl, { fetchOptions });
    }
}
