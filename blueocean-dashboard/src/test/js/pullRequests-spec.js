import React from 'react';
import { assert} from 'chai';
import { shallow } from 'enzyme';
import { latestRuns as branches } from './data/runs/latestRuns';

import {PullRequests} from '../../main/js/components/PullRequests.jsx';

const pr = branches.filter((run) => run.pullRequest);

describe("PullRequests should render", () => {
  it("does renders the PullRequests with data", () => {
    const wrapper =  shallow(<PullRequests pullRequests={pr} />);
    // does data renders?
    assert.equal(wrapper.find('PullRequest').length, pr.length);
    const table = wrapper.find('Table').node;
    assert.isOk(table);
    assert.include(table.props.className, 'pr-table');
  });

});

describe("PullRequests should not render", () => {
  it("does render NotSupported the PullRequests without data", () => {
    const wrapper =  shallow(<PullRequests />);
    assert.equal(wrapper.find('NotSupported').length, 1);
  });

});
