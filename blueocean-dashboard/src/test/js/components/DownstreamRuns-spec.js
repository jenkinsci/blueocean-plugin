import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';
import { DownstreamRuns } from '../../../main/js/components/downstream-runs/DownstreamRuns';
import { DownstreamRunsView } from '../../../main/js/components/downstream-runs/DownstreamRunsView';

describe('Downstream Runs', () => {
    it('constructs without any env', () => {
        const wrapper = shallow(<DownstreamRuns />);

        assert.equal(wrapper.length, 1);
        assert.equal(wrapper.text(), '', 'renders nothing');
        assert.equal(wrapper.html(), null, 'renders nothing');
    });

    describe('When there are downstream runs', () => {
        const downstreamRunURLs = [
            {
                runLink: '/blue/rest/organizations/jenkins/pipelines/downstream1/runs/105/',
                runDescription: 'downstream1 #105',
            },
            {
                runLink: '/blue/rest/organizations/jenkins/pipelines/downstream1/runs/106/',
                runDescription: 'downstream1 #106',
            },
        ];

        it('requests data if needed', () => {
            const requestedURLs = [];

            const activityService = {
                getItem(ignored) {
                    return null;
                },
                setItem(result) {
                    // ignore
                },
                fetchActivity(url, options) {
                    requestedURLs.push(url);
                    return Promise.resolve('some value');
                },
            };

            const wrapper = shallow(<DownstreamRuns runs={downstreamRunURLs} />, { context: { activityService } });

            assert.equal(wrapper.length, 1);
            assert.equal(requestedURLs.length, 2, 'requested URLs');
        });

        it('renders data if available', () => {
            const requestedURLs = [];
            const cachedResult = 'cached result';

            const activityService = {
                getItem(url) {
                    return cachedResult;
                },
                setItem(result) {
                    // ignore
                },
                fetchActivity(url, options) {
                    requestedURLs.push(url);
                    return Promise.resolve('some value');
                },
            };

            const wrapper = shallow(<DownstreamRuns runs={downstreamRunURLs} />, { context: { activityService } });

            assert.equal(wrapper.length, 1);
            assert.equal(requestedURLs.length, 0, 'no requested URLs');

            const child = wrapper.at(0);

            assert.equal(child.node.type, DownstreamRunsView, 'child is a DownstreamRunsView');
            assert(Array.isArray(child.node.props.runs), 'has runs prop and is array');
            assert.equal(child.node.props.runs.length, 2, '2 runs');
            assert.equal(child.node.props.runs[0].runDetails, cachedResult, 'first value');
            assert.equal(child.node.props.runs[1].runDetails, cachedResult, 'second value');
        });
    });
});
