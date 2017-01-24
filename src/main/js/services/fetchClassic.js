import { Fetch, UrlConfig } from '@jenkins-cd/blueocean-core-js';

export default function fetch(path, body, handler) {
    Fetch.fetch(`${UrlConfig.getJenkinsRootURL()}/blue/rest/pipeline-metadata/crumbInfo`, {
        fetchOptions: { method: 'GET' }
    }).then(response => {
        if (!response.ok) {
            console.log('An error happened fetching ', path);
            return;
        }
        const useCrumb = function (crumb) {
            const headers = {
                'Content-Type': 'application/x-www-form-urlencoded'
            };
            if (crumb.indexOf('=') > 0) {
                crumb = crumb.split('=');
                headers[crumb[0]] = crumb[1];
            }
            Fetch.fetchJSON(UrlConfig.getJenkinsRootURL() + path, {
                fetchOptions: {
                    method: 'POST',
                    body: body,
                    headers: headers,
                }
            }).then(data => {
                if (data.status === 'ok') {
                    handler(data.data);
                } else {
                    console.log(data);
                }
            });
        };
        let crumb = response.text();
        if (crumb instanceof Promise) {
            crumb.then(useCrumb);
        } else {
            useCrumb(crumb);
        }
    });
}

