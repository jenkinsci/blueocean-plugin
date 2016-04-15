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
    assert.isNotNull(statusindicator);
    assert.equal(statusindicator.props.result, 'success');
    assert.equal(statusindicator.props.width, props.width);
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
    assert.equal(circle.props.fill, results.failure.fill);
    assert.equal(circle.props.stroke, results.failure.stroke);
  });

});

describe("SvgSpinner should render", () => {
  let tree = null;

  beforeEach(() => {
    tree = sd.shallowRender(<SvgSpinner
         result="QUEUED"
    />);
  });

  it("does render FAILURE", () => {
    const path = tree.subTree('path').getRenderOutput();
    assert.isNotNull(path);
    assert.equal(path.props.fill, 'none');
    assert.equal(path.props.stroke, '#4a90e2');
  });

});
