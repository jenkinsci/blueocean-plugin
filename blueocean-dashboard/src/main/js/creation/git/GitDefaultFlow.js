/**
 * Created by cmeyers on 10/21/16.
 */

import React, { PropTypes } from 'react';

import MultiStepFlow from '../MultiStepFlow';

import ConnectStep from './ConnectStep';
import CompletedStep from './CompletedStep';

export default class GitDefaultFlow extends React.Component {
    render() {
        return (
            <MultiStepFlow {...this.props}>
                <ConnectStep />
                <CompletedStep />
            </MultiStepFlow>
        );
    }
}
