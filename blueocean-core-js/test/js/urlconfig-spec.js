import { assert } from 'chai';

import { UrlConfig } from '../../src/js/urlconfig';

function setAppUrl(url) {
    const headElement = document.getElementsByTagName('head')[0];

    if (url === null) {
        headElement.removeAttribute('data-appurl');
    } else {
        headElement.setAttribute('data-appurl', url);
    }
}

describe('urlconfig', () => {
    beforeEach(() => {
        UrlConfig.enableReload();
    });

    describe('getRestBaseURL', () => {
        it('should build the proper URL when "data-appurl" is supplied', () => {
            setAppUrl('/jenkins/blue');
            assert.equal(UrlConfig.getRestBaseURL(), '/jenkins/blue/rest');
        });
    });

    afterEach(() => {
        setAppUrl(null);
    });
});
