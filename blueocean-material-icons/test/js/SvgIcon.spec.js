/* eslint-env mocha */
import React, {Component, PropTypes } from 'react';
import {spy} from 'sinon';
import {shallow} from 'enzyme';
import {assert} from 'chai';
import SvgIcon from '../../src/js/components/SvgIcon/SvgIcon';

describe('<SvgIcon />', () => {
  const shallowWithContext = (node) => shallow(node);
  const path = <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" />;

  it('renders children by default', () => {
    const wrapper = shallowWithContext(
      <SvgIcon>{path}</SvgIcon>
    );

    assert.ok(wrapper.contains(path), 'should contain the children');
  });

  it('renders children and overwrite styles', () => {
    const wrapper = shallowWithContext(
      <SvgIcon style={{"fill": "red"}}>{path}</SvgIcon>
    );

    assert.ok(wrapper.contains(path), 'should contain the children');
    assert.equal(wrapper.node.props.style.fill, 'red', 'should have color set to red');
  });

  it('renders children and call onMouseEnter callback', () => {
    const onMouseEnter = spy();
    const wrapper = shallowWithContext(
      <SvgIcon onMouseEnter={onMouseEnter}>{path}</SvgIcon>
    );

    assert.ok(wrapper.contains(path), 'should contain the children');
    wrapper.simulate('mouseEnter');
    assert.equal(onMouseEnter.calledOnce, true,
      'should have called onMouseEnter callback function');
  });

  it('renders children and call onMouseLeave callback', () => {
    const onMouseLeave = spy();
    const wrapper = shallowWithContext(
      <SvgIcon onMouseLeave={onMouseLeave}>{path}</SvgIcon>
    );

    assert.ok(wrapper.contains(path), 'should contain the children');
    wrapper.simulate('mouseLeave');
    assert.equal(onMouseLeave.calledOnce, true,
      'should have called onMouseLeave callback function');
  });
});
