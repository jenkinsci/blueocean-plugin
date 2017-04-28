import { assert } from 'chai';

import { LocationService } from '../../../src/js/services/LocationService';

/**
 * Utility for creating location object passed to history.listen
 * @param {string} action
 * @param {string} pathname
 * @returns {Object}
 */
function createLocation(pathname, action = 'PUSH') {
    return {
        action,
        pathname,
        search: '',
        hash: '',
        state: null,
        key: 'mdrv0s',
        basename: '/jenkins/blue',
        query: {},
        $searchBase: {
            search: '',
            searchBase: '',
        },
    };
}

describe('LocationService', () => {
    let locationService;

    beforeEach(() => {
        locationService = new LocationService();
    });

    it('sets current after a route added', () => {
        const loc = '/pipelines';
        locationService.setCurrent(
            createLocation(loc)
        );

        assert.isNotOk(locationService.previous);
        assert.equal(locationService.current, loc);
    });

    it('sets previous after two routes added', () => {
        const loc = '/pipelines';
        locationService.setCurrent(
            createLocation(loc)
        );

        const loc2 = '/organizations/jenkins/pipelines';
        locationService.setCurrent(
            createLocation(loc2)
        );

        assert.equal(locationService.previous, loc);
        assert.equal(locationService.current, loc2);
    });

    it('handles the REPLACE scenario', () => {
        const loc = '/pipelines';
        locationService.setCurrent(
            createLocation(loc)
        );

        const loc2 = '/organizations/jankins/pipeline-1/activity';
        locationService.setCurrent(
            createLocation(loc2)
        );

        const loc3 = '/organizations/jenkins/pipelines';
        locationService.setCurrent(
            createLocation(loc3, 'REPLACE')
        );

        assert.equal(locationService.previous, loc);
        assert.equal(locationService.current, loc3);
    });
});
