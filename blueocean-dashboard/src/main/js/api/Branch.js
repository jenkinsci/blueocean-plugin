/**
 * Simple pipeline branch API component.
 * <p>
 * Non-react component that contains general API methods for
 * interacting with pipeline branches, encapsulating REST API calls etc.
 */

// import fetch from 'isomorphic-fetch';
import config from '../config';
import Pipeline from './Pipeline';
import * as pushEventUtil from '../util/push-event-util';

export default class Branch {

    constructor(pipeline, name) {
        this.pipeline = pipeline;
        this.name = name;
    }

    run() {
        // TODO: fix this when JENKINS-35797 is done
        const url = `${config.blueoceanAppURL}/rest/organizations/${this.pipeline.organization}/pipelines/${this.pipeline.name}/branches/${this.name}/startRun`;

        // TODO: isomorphic-fetch not working for posts?
        // Is supposed to be extended from whatwg-fetch.
        // See https://www.npmjs.com/package/whatwg-fetch#post-json
        //
        // fetch(url, {
        //    method: 'post',
        //    headers: {
        //        'Content-Type': 'application/json',
        //    },
        // }).then((response) => {
        //    console.log(response);
        // });

        // Just use XMLHttpRequest for now. See above.
        // eslint-disable-next-line
        post(url);
    }

    equals(branch) {
        if (branch && branch.name === this.name) {
            // and it's the same pipeline...
            return (
                branch.pipeline.organization === this.pipeline.organization &&
                branch.pipeline.name === this.pipeline.name
            );
        }
        return false;
    }
}

exports.fromSSEEvent = function (event) {
    const eventCopy = pushEventUtil.enrichJobEvent(event);
    if (!eventCopy.blueocean_is_multi_branch) {
        return undefined;
    }
    return new Branch(
        new Pipeline('jenkins', eventCopy.blueocean_job_name),
        eventCopy.blueocean_branch_name
    );
};

// TODO: remove once we find out why isomorphic-fetch is not working
function post(toUrl) {
    const http = new XMLHttpRequest();

    http.onreadystatechange = function () {
        if (http.readyState === 4) {
            if (http.status >= 200 && http.status < 300) {
                // Not a JSON response.
            } else {
                console.error(`Error invoking Job run via ${toUrl}: `);
                console.error(http);
            }
        }
    };

    http.open('POST', toUrl, true);
    http.setRequestHeader('Content-type', 'application/json');
    http.send();
}
