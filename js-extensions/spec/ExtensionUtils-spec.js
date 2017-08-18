const assert = require('chai').assert;

const Utils = require('../dist/ExtensionUtils.js').default;
const { sortByOrdinal } = Utils;

describe("ExtensionUtils", () => {
    describe('sortByOrdinal', () => {
        it('should sort ordinals in asc order', () => {
            const extensions = [
                { pluginId: 'B', ordinal: 200 },
                { pluginId: 'A', ordinal: 100 },
            ];

            const sorted = sortByOrdinal(extensions);
            assert.equal(sorted[0].pluginId, 'A');
            assert.equal(sorted[1].pluginId, 'B');
        });
        it('should sort pluginId in asc order', () => {
            const extensions = [
                { pluginId: 'B' },
                { pluginId: 'A' },
            ];

            const sorted = sortByOrdinal(extensions);
            assert.equal(sorted[0].pluginId, 'A');
            assert.equal(sorted[1].pluginId, 'B');
        });
        it('should sort by ordinal first, then by pluginId in asc order', () => {
            const extensions = [
                { pluginId: 'D' },
                { pluginId: 'C' },
                { pluginId: 'B', ordinal: 200 },
                { pluginId: 'A', ordinal: 100 },
            ];

            const sorted = sortByOrdinal(extensions);
            console.log(sorted);
            assert.equal(sorted[0].pluginId, 'A');
            assert.equal(sorted[1].pluginId, 'B');
            assert.equal(sorted[2].pluginId, 'C');
            assert.equal(sorted[3].pluginId, 'D');
        });
    });
});
