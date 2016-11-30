

/**
 * Trims duplicate forward slashes to a single slash and adds trailing slash if needed.
 * @param url
 * @param trailingSlash
 * @returns {string}
 */
const cleanSlashes = (url, trailingSlash = true) => {
    let cleanUrl = url;

    while (cleanUrl.indexOf('//') !== -1) {
        cleanUrl = cleanUrl.replace('//', '/');
    }

    if (trailingSlash && cleanUrl.substr(-1) !== '/') {
        return `${cleanUrl}/`;
    }

    return cleanUrl;
};

export default {
    cleanSlashes,
    clone(obj) {
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
