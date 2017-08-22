import { Fetch, UrlConfig } from '@jenkins-cd/blueocean-core-js';

const cache = {};

export default function fetch(path, body, handler, disableLoadingIndicator) {
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
                disableLoadingIndicator: disableLoadingIndicator,
            }
        }).then(data => {
            if (data.status === 'ok') {
                handler(data.data);
            }
        });
    };
    if (cache.crumb) {
        useCrumb(cache.crumb);
    } else {
        Fetch.fetch(`${UrlConfig.getJenkinsRootURL()}/blue/rest/pipeline-metadata/crumbInfo`, {
            fetchOptions: { method: 'GET', disableLoadingIndicator: disableLoadingIndicator }
        }).then(response => {
            if (!response.ok) {
                if (window.isDevelopmentMode) console.error('An error occurred while fetching:', path);
                throw response;
            }
    
            if (cache.crumb) {
                useCrumb(cache.crumb);
            } else {
                try {
                    let crumb = response.text();
                    if (crumb.then) {
                        crumb.then(c => {
                            cache.crumb = c;
                            useCrumb(c);
                        })
                        .catch(err => {
                            fetch(path, body, handler, disableLoadingIndicator);
                        });
                    } else {
                        cache.crumb = crumb;
                        useCrumb(crumb);
                    }
                } catch(e) {
                    fetch(path, body, handler, disableLoadingIndicator);
                }
            }
        });
    }
}

