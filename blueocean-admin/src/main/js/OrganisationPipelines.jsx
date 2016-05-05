import React, { Component, PropTypes } from 'react';
import { actions, pipelines as pipelinesSelector, connect, createSelector } from './redux';

class OrganisationPipelines extends Component {

    getChildContext() {
        const {
            params,
            location,
            pipelines,
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

    componentWillMount() {
        if (this.context.config) {
            const url = `${this.context.config.getAppURLBase()}` +
                '/rest/organizations/jenkins/pipelines/';
            this.props.fetchPipelinesIfNeeded(url);
        }
    }

    render() {
        return this.props.children; // Set by router
    }
}

OrganisationPipelines.contextTypes = {
    router: PropTypes.object.isRequired,
    config: PropTypes.object.isRequired,
};

OrganisationPipelines.propTypes = {
    fetchPipelinesIfNeeded: PropTypes.func.isRequired,
    params: PropTypes.object, // From react-router
    children: PropTypes.node, // From react-router
    location: PropTypes.object, // From react-router
    pipelines: PropTypes.array,
};

OrganisationPipelines.childContextTypes = {
    pipelines: PropTypes.array,
    pipeline: PropTypes.object,
    params: PropTypes.object, // From react-router
    location: PropTypes.object, // From react-router
};

const selectors = createSelector([pipelinesSelector], (pipelines) => ({ pipelines }));

export default connect(selectors, actions)(OrganisationPipelines);
