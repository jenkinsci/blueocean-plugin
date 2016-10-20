/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';

import MultiStepFlow from '../MultiStepFlow';

import ConnectStep from './ConnectStep';
import CompletedStep from './CompletedStep';

export default class GitScmSteps extends React.Component {
    render() {
        return (
            <MultiStepFlow {...this.props}>
                <ConnectStep />
                <CompletedStep />
            </MultiStepFlow>
        );
    }
}
