import React, { Component, PropTypes } from 'react';
import { fetch } from '@jenkins-cd/design-language';
import { actions, pipelines } from './redux';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';
import { rootRoutePath, urlPrefix } from  './config';

class OrganisationPipelines extends Component {
    componentWillMount() {
      this.props.generatePipelineData();
    }
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
    generatePipelineData: PropTypes.func.isRequired,
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
const selectors = createSelector([pipelines], (pipelines) => ({
        pipelines
    })
);

export default connect(selectors, actions)(OrganisationPipelines);
// eslint-disable-next-line
//export default fetch(OrganisationPipelines, (props, config) =>
//     `${config.getAppURLBase()}/rest/organizations/jenkins/pipelines/`);

