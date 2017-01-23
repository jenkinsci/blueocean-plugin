
export default {
    // TODO: for now, just validate with have a string of some kind
    validateUrl(url) {
        return !!url && !!url.trim();
    },
};
