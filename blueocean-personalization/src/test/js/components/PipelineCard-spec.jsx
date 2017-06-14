/**
 * Created by cmeyers on 7/6/16.
 */
import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { mockExtensionsForI18n } from '../mock-extensions-i18n';
import { PipelineCard, PipelineCardRenderer } from '../../../main/js/components/PipelineCard';

function clone(object) {
    return JSON.parse(JSON.stringify(object));
}

const context = {
    params: {},
    config: {
        getServerBrowserTimeSkewMillis: () => 0,
    },
};

// Dummy translation
const t = (key, options) => options && options.defaultValue || key;
t.lng = 'EN';

describe('PipelineCard', () => {
    let item;
    let favorite;

    beforeEach(() => {
        const favorites = clone(require('../data/favorites.json'));

        item = favorites[0].item;
        item._capabilities = ['io.jenkins.blueocean.rest.model.BlueBranch'];
        favorite = true;

        mockExtensionsForI18n();
    });

    it('renders without error for empty props', () => {
        const wrapper1 = shallow(
            <PipelineCard t={t} />,
            { context },
        );

        assert.isOk(wrapper1);
    });

    it('renders with correct props', () => {
        item.latestRun.result = 'SUCCESS';
        const wrapper = shallow(
            <PipelineCard runnable={item} favorite={favorite} t={t} />,
            { context }
        );

        assert.equal(wrapper.find('PipelineCardRenderer').length, 1);
        assert.equal(wrapper.prop('status'), 'SUCCESS');
        assert.equal(wrapper.prop('startTime'), '2016-05-24T08:57:04.432-0400');
        assert.equal(wrapper.prop('estimatedDuration'), 73248);
        assert.equal(wrapper.prop('activityUrl'), '/organizations/jenkins/blueocean/activity');
        assert.equal(wrapper.prop('displayPath'), 'blueocean');
        assert.equal(wrapper.prop('branchText'), 'UX-301');
        assert.equal(wrapper.prop('commitText'), 'cfca303');
        assert.equal(wrapper.prop('favoriteChecked'), true);
        assert.equal(typeof wrapper.prop('runnableItem'), 'object');
        assert.equal(typeof wrapper.prop('latestRun'), 'object');
        assert.equal(typeof wrapper.prop('timeText'), 'object');
        assert.equal(typeof wrapper.prop('onFavoriteToggle'), 'function');
        assert.equal(typeof wrapper.prop('onRunDetails'), 'function');
        assert.equal(typeof wrapper.prop('onClickMain'), 'function');
    });
});
