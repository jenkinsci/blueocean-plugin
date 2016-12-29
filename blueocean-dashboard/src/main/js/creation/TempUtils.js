
function cleanSlashes(url, trailingSlash = true) {
    let cleanUrl = url;

    while (cleanUrl.indexOf('//') !== -1) {
        cleanUrl = cleanUrl.replace('//', '/');
    }

    if (trailingSlash && cleanUrl.substr(-1) !== '/') {
        return `${cleanUrl}/`;
    }

    return cleanUrl;
}

export default {
    cleanSlashes,
};
