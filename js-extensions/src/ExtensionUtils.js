/**
 * Sort extensions by ordinal if defined, then fallback to pluginId.
 * @param extensions
 * @param [done]
 */
function sortByOrdinal(extensions, done) {
    const sorted = extensions.sort((a, b) => {
        if (a.ordinal || b.ordinal) {
            if (!a.ordinal) return 1;
            if (!b.ordinal) return -1;
            if (a.ordinal < b.ordinal) return -1;
            return 1;
        }
        return a.pluginId.localeCompare(b.pluginId);
    });

    if (done) {
        done(sorted);
    }

    return sorted;
}

export default {
    sortByOrdinal,
};
