/**
 * Generate a "unique" ID with an optional prefix
 * @param prefix
 * @returns {string}
 */
function randomId(prefix: string = 'id'): string {
    const integer: number = Math.round(Math.random() * Number.MAX_SAFE_INTEGER);
    return `${prefix}-${integer}`;
}

export default {
    randomId,
};
