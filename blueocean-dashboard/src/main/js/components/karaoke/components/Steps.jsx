import React, { Component, PropTypes } from 'react';
import { Step } from './Step';

export default class Steps extends Component {
    render() {
        const { nodeInformation } = this.props;
        // Early out
        if (!nodeInformation) {
            return null;
        }
        const { model } = nodeInformation;
        return (<div>
            { model.map((item) => <Step
                { ...
                    { ...this.props,
                        key: item.key,
                        step: item,
                    }
                }
            />) }
        </div>);
    }
}

Steps.propTypes = {
    nodeInformation: PropTypes.object.isRequired,
};

