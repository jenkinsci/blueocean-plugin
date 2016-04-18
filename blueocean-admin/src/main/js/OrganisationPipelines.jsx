import React, { Component, PropTypes } from 'react';
import { fetch } from '@jenkins-cd/design-language';

class OrganisationPipelines extends Component {

    getChildContext() {
        const {
            params,
            location,
            data: pipelines,
        } = this.props;

        // The specific pipeline we may be focused on
        let pipeline;

        if (pipelines && params && params.pipeline) {
            const name = params.pipeline;
            pipeline = pipelines.find(aPipeLine => aPipeLine.name === name);
        }

        return {
            pipelines,
            pipeline,
            params,
            location,
        };
    }

    render() {
        return this.props.children; // Set by router
    }
}

OrganisationPipelines.contextTypes = {
    router: React.PropTypes.object.isRequired,
};

OrganisationPipelines.propTypes = {
    data: PropTypes.array, // From Ajax wrapper
    params: PropTypes.object, // From react-router
    children: PropTypes.node, // From react-router
    location: PropTypes.object, // From react-router
};

OrganisationPipelines.childContextTypes = {
    pipelines: PropTypes.array,
    pipeline: PropTypes.object,
    params: PropTypes.object, // From react-router
    location: PropTypes.object, // From react-router
};

// eslint-disable-next-line
export default fetch(OrganisationPipelines, (props, config) =>
     `${config.getAppURLBase()}/rest/organizations/jenkins/pipelines/`);

