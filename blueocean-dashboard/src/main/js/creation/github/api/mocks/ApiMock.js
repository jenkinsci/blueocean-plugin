import Promise from 'bluebird';
import { Utils } from '@jenkins-cd/blueocean-core-js';

export class ApiMock {
    _delayedResolve(payload, delay = 500) {
        return new Promise(resolve => {
            setTimeout(() => {
                console.log('resolve:', payload);
                resolve(Utils.clone(payload));
            }, delay);
        });
    }

    _delayedReject(payload, delay = 500) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const response = {
                    responseBody: payload,
                };
                console.log('reject:', response);
                reject(Utils.clone(response));
            }, delay);
        });
    }

    _hasUrlKey(keyValue) {
        return (
            window.location.href
                .split('?')
                .slice(-1)
                .join('')
                .indexOf(keyValue) !== -1
        );
    }
}
