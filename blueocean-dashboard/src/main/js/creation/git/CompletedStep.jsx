/**
 * Created by cmeyers on 10/19/16.
 */
import React from 'react';

import FlowStep from '../FlowStep';

export default class CompletedStep extends React.Component {
    render() {
        return (
            <FlowStep {...this.props} title="Completed">

            </FlowStep>
        );
    }
}

CompletedStep.propTypes = {};
