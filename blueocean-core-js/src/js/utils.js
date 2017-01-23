// @flow

/**
 * Trims duplicate forward slashes to a single slash and adds trailing slash if needed.
 * @param url
 * @returns {string}
 */
function cleanSlashes(url: string) {
    let baseUrl = '';
    let urlParams = '';

    if (url && url.indexOf('?') > -1) {
        baseUrl = url.split('?').slice(0, 1).join('');
        urlParams = url.split('?').slice(-1).join('');
    } else {
        baseUrl = url;
    }

    // replace any number of consecutive slashes with one slash
    baseUrl = baseUrl.replace(/\/\/+/g, '/');

    if (baseUrl.substr(-1) !== '/') {
        baseUrl = `${baseUrl}/`;
    }

    return !urlParams ? baseUrl : `${baseUrl}?${urlParams}`;
}


export default {
    cleanSlashes,
    clone(obj: Object) {
        if (!obj) return obj;
        return JSON.parse(JSON.stringify(obj));
    },
    windowOrGlobal() {
        return (typeof self === 'object' && self.self === self && self) ||
  (typeof global === 'object' && global.global === global && global) ||
  this;
    },
    refreshPage() {
        if (this.windowOrGlobal().location.reload) {
            this.windowOrGlobal().location.reload(true);
        }
    },
};
