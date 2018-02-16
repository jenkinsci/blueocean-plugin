import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import VerticalStep from '../../../../main/js/creation/flow2/VerticalStep';
import FlowStepStatus from '../../../../main/js/creation/flow2/FlowStepStatus';


describe('VerticalStep', () => {
    it('should enforce lowercase status classnames', () => {

        const wrapper = shallow(
            <VerticalStep status={FlowStepStatus.ACTIVE} />
        );

        assert.isTrue(wrapper.hasClass('active'), 'class name must be "active" lowercase');
    });
    it('should add "last-step" classname', () => {

        const wrapper = shallow(
            <VerticalStep isLastStep />
        );

        assert.isTrue(wrapper.hasClass('last-step'), 'class name must include "last-step"');
    });
});
