//FIXME: should be part of the jdl, but test are broken there
import React from 'react';
import { assert} from 'chai';
import sd from 'skin-deep';
import Immutable from 'immutable';

import { StatusIndicator, SvgStatus, SvgSpinner }
    from '@jenkins-cd/design-language';

const props = {
    width: '640px',
    height: '640px',
};
const results = {
    failure: {
        fill: '#d54c53',
        stroke: '#cf3a41',
    },
};

describe("StatusIndicator should render", () => {
  let tree = null;

  beforeEach(() => {
    tree = sd.shallowRender(<StatusIndicator
        result="SUCCESS"
        {...props}
    />);
  });

  it("does render success", () => {
    const statusindicator = tree.getRenderOutput();
    assert.isNotNull(statusindicator, 'tree.getRenderOutput()');
    assert.equal(statusindicator.props.width, props.width, 'width prop');

    assert.isOk(tree.subTree('title'), 'contains a <title> element');
    assert.isOk(tree.subTree('g'), 'contains a <g> element');
  });

});

describe("SvgStatus should render", () => {
  let tree = null;

  beforeEach(() => {
    tree = sd.shallowRender(<SvgStatus
        result="FAILURE"
    />);
  });

  it("does render FAILURE", () => {
    const circle = tree.subTree('circle').getRenderOutput();
    assert.isNotNull(circle);
  });

});

describe("SvgSpinner should render", () => {
  let tree = null;

  beforeEach(() => {
    tree = sd.shallowRender(<SvgSpinner
         result="RUNNING"
         percentage={40}
    />);
  });

  it("does render RUNNING", () => {
    assert.isOk(tree.subTree('circle'), 'contains a <circle> element');
    assert.isOk(tree.subTree('path'), 'contains a <path> element');
  });

});
