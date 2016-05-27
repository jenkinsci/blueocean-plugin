import React, { Component, PropTypes } from 'react';
import Node from './Node';

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
        return (<div>
            {
              model.map((item, index) =>
                <Node
                  key={index}
                  node={item}
                  nodesBaseUrl={nodesBaseUrl}
                />)
            }
        </div>);
    }
}

Nodes.propTypes = {
    nodeInformation: PropTypes.object.isRequired,
};
