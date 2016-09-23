/**
 * Created by cmeyers on 7/15/16.
 */
import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { DashboardCards } from '../../../main/js/components/DashboardCards';
import { CapabilityTestUtils } from '../CapabilityTestUtils';

describe('DashboardCards', () => {
    let favorites;
    let testUtils;

    beforeEach((done) => {
        favorites = require('../data/favorites.json');

        testUtils = new CapabilityTestUtils();
        testUtils.bindCapability(
            'io.jenkins.blueocean.rest.impl.pipeline.BranchImpl',
            'io.jenkins.blueocean.rest.model.BlueBranch'
        );
        testUtils.augment(favorites);

        done();
    });

    afterEach(() => {
        testUtils.unbindAll();
    });

    it('renders without error for empty props', () => {
        const wrapper = shallow(
            <DashboardCards />
        );

        assert.isOk(wrapper);
    });
});
