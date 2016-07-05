import React, { Component, PropTypes } from 'react';
import Step from './Step';

export default class Nodes extends Component {
    render() {
        const { nodeInformation } = this.props;
        // Early out
        if (!nodeInformation) {
            return null;
        }
        const {
            model,
            nodesBaseUrl,
        } = nodeInformation;
        console.log('renderSteps', model, nodesBaseUrl);
        return (<div>
            {
              model.map((item, index) =>
                <Step
                  key={index}
                  node={item}
                  nodesBaseUrl={nodesBaseUrl}
                  {...this.props}
                />)
            }
        </div>);
    }
}

Nodes.propTypes = {
    nodeInformation: PropTypes.object.isRequired,
};
