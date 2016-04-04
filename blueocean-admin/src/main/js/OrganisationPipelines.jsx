import React, { Component, PropTypes } from 'react';
import AjaxHoc from './AjaxHoc';


class OrganisationPipelines extends Component {

    getChildContext() {
        // All the pipelines we're interested in
        const pipelines = this.props.data;

        // The specific pipeline we may be focused on
        let pipeline;

        if(pipelines && this.props.params && this.props.params.pipeline) {
            const name = this.props.params.pipeline;
            pipeline = pipelines.find(aPipeLine => aPipeLine.get("name") == name);
            // FIXME: This foo.get("bar") syntax is not ideal ^^^

            // Convert back to a real JS object
            if (pipeline) pipeline = pipeline.toJS();
        }

        return { pipelines, pipeline };
    }

    render() {
        return this.props.children; // Set by router
    }
}

OrganisationPipelines.propTypes = {
    data: PropTypes.object, // From Ajax wrapper
    params: PropTypes.object, // From react-router
    children: PropTypes.node // From react-router
};

OrganisationPipelines.childContextTypes = {
    pipelines: PropTypes.object,
    pipeline: PropTypes.object
};

// eslint-disable-next-line
export default AjaxHoc(OrganisationPipelines, (props, config) =>
     `${config.getAppURLBase()}/rest/organizations/jenkins/pipelines/`);
