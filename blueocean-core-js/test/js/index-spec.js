import { assert } from 'chai';

import * as corejs from '../../src/js';

/**
 * Basically a sanity check for out exports, to catch typos or stuff we miss during refactors.
 */

describe('@jenkins-cd/blueocean-core-js', () => {
    // Check the main index

    checkModuleExports('index', corejs, [
        'sseConnection', // Because @jenkins-cd/sse-gateway gives us a valid connect() function during tests, but that function returns null
    ]);

    // Check exports that are namespaces, rather than components / classes.
    // Overkill for exported declarations, designed to catch re-exported imports or module-scoped consts.

    checkModuleExports('UrlBuilder', corejs.UrlBuilder);
    checkModuleExports('UrlUtils', corejs.UrlUtils);
    checkModuleExports('FetchFunctions', corejs.FetchFunctions);
    checkModuleExports('Fetch', corejs.Fetch);
    checkModuleExports('UrlConfig', corejs.UrlConfig);
    checkModuleExports('JWT', corejs.JWT);
    checkModuleExports('TestUtils', corejs.TestUtils);
    checkModuleExports('ToastUtils', corejs.ToastUtils);
    checkModuleExports('Utils', corejs.Utils);
    checkModuleExports('AppConfig', corejs.AppConfig);
    checkModuleExports('Paths', corejs.Paths);
    checkModuleExports('StringUtil', corejs.StringUtil);
});

function checkModuleExports(label, exported, exclusions = []) {
    describe(`module "${label}"`, () => {
        it('exists', () => {
            assert.isOk(exported);
        });

        it('does not export anything as undefined', () => {
            if (!exported) {
                return; // Tested above in 'exists' with a nicer message
            }

            const badExports = [];
            for (const name of Object.keys(exported)) {
                if (exclusions.indexOf(name) !== -1) {
                    continue;
                }
                if (typeof exported[name] === 'undefined') {
                    badExports.push(name);
                }
            }

            assert.equal(badExports.length, 0, `The following exported symbols are undefined: ${badExports.join(', ')}`);
        });

        describe('index-spec.js', () => {
            // Keep these tests in order also. We're a bit meta here, sorry :-/
            it('has not specified any bad exclusions', () => {
                for (const name of exclusions) {
                    const propType = typeof exported[name];
                    assert.equal(propType, 'undefined', `${name} is exluded from comparison but actually has type ${propType}`);
                    assert.isTrue(name in exported, 'undefined', `${name} is exluded from comparison but is not exported and should be removed`);
                }
            });
        });
    });
}
