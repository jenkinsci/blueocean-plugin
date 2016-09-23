/**
 * Created by cmeyers on 9/23/16.
 */
import { Fetch } from '../fetch';
import config from '../urlconfig';
import utils from '../utils';

export class PipelineApi {

    fetchLatestRun(jobUrl) {
        const path = config.getJenkinsRootURL();
        const fullJobUrl = utils.cleanSlashes(`${path}/${jobUrl}`);
        return Fetch.fetchJSON(fullJobUrl)
            .then(data => data.latestRun);
    }

}
