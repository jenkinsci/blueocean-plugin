/**
 * Created by cmeyers on 9/29/16.
 */

import { Fetch, UrlConfig, Utils } from '@jenkins-cd/blueocean-core-js';

export class FavoritesApi {

    fetchFavorites(user) {
        const path = UrlConfig.getJenkinsRootURL();
        const queueUrl = Utils.cleanSlashes(`${path}/${user._links.self.href}/favorites/`);
        return Fetch.fetchJSON(queueUrl);
    }

}
