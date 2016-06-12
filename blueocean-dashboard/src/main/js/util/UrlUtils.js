/**
 * Trim the last path segment off a URL and return it.
 * Handles trailing slashes nicely.
 * @param url
 * @returns {string}
 */
export const removeLastUrlSegment = (url) => {
    const paths = url.split('/').filter(path => (path.length > 0));
    paths.pop();
    return paths.join('/');
};
