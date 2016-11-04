// @flow 

/**
 * Trims duplicate forward slashes to a single slash and adds trailing slash if needed.
 * @param url
 * @returns {string}
 */
const cleanSlashes = (url: string) => {
    if (url.indexOf('//') !== -1) {
        let cleanUrl = url.replace('//', '/');
        cleanUrl = cleanUrl.substr(-1) === '/' ?
            cleanUrl : `${cleanUrl}/`;

        return cleanSlashes(cleanUrl);
    }

    return url;
};

export default {
    cleanSlashes,
    clone(obj: Object) {
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
