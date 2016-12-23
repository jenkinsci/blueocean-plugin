import React from 'react';
import { assert} from 'chai';
import { shallow } from 'enzyme';
import { latestRuns as branches } from './data/runs/latestRuns';

import {PullRequests} from '../../main/js/components/PullRequests.jsx';

const pr = branches.filter((run) => run.pullRequest);
const pipeline = {
    _class: 'someclass',
    _capabilities: ['io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline'],
};
const t = () => {};

const context = {
    pipelineService: {
      prPager() {
        return {
          data: pr,
        };
      }
    }
}
const params = {}
describe("PullRequests should render", () => {
  it("does renders the PullRequests with data", () => {
    const wrapper = shallow(<PullRequests t={t} pipeline={pipeline} params={params} />, { context });


    // does data renders?
    assert.equal(wrapper.find('PullRequest').length, pr.length);
    const table = wrapper.find('Table').node;
    assert.isOk(table);
    assert.include(table.props.className, 'pr-table');
  });

});

describe("PullRequests should not render", () => {
  it("does render NotSupported the PullRequests without data", () => {
    const wrapper =  shallow(<PullRequests t={t} />);
    assert.equal(wrapper.find('NotSupported').length, 1);
  });

});
