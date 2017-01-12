// import validateUrl from './regex-weburl';

export default {
    // TODO: maybe we want a better way to validate this URL but there are too many permutations to obsess about it now.
    validateUrl(url) {
        return !!url && !!url.trim();
    },
};
