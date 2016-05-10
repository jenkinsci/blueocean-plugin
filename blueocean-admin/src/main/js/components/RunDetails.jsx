import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    PipelineResult,
    PageTabs,
    TabLink,
} from '@jenkins-cd/design-language';

import {
    actions,
    currentRuns as runsSelector,
    isMultiBranch as isMultiBranchSelector,
    createSelector,
    connect,
} from '../redux';

const { func, object, array, any } = PropTypes;

/**
 * Trim the last path element off a URL. Handles trailing slashes nicely.
 * @param url
 * @returns {string}
 */
function cleanBaseUrl(url) {
    const paths = url.split('/').filter(path => (path.length > 0));
    paths.pop();
    return paths.join('/');
}

class RunDetails extends Component {
    constructor(props) {
        super(props);

        this.navigateToChanges = this.navigateToChanges.bind(this);
    }
    componentWillMount() {
        if (this.context.config && this.context.params) {
            const {
                params: {
                    pipeline,
                },
                config = {},
            } = this.context;
            config.pipeline = pipeline;
            this.props.fetchRunsIfNeeded(config);
            this.props.setPipeline(config);
        }
    }
    navigateToChanges() {
        const changesUrl = `${cleanBaseUrl(this.context.location.pathname)}/changes`;
        this.context.router.push(changesUrl);
    }
    render() {
        // early out
        if (!this.context.params
            || !this.props.runs
            || this.props.isMultiBranch === null) {
            return null;
        }
        const {
            router,
            params,
        } = this.context;

        const { pipeline: name, branch, runId } = params; // From route

        const baseUrl = cleanBaseUrl(this.context.location.pathname);

        const result = this.props.runs.filter(
            (run) => run.id === runId && decodeURIComponent(run.pipeline) === branch)[0];

        result.name = name;

        const afterClose = () => {
            router.goBack();
        };

        return (<ModalView
          isVisible
          result={result.result}
          {...{ afterClose }}
        >
            <ModalHeader>
                <div>
                    <PipelineResult data={result}
                      onAuthorsClick={this.navigateToChanges}
                    />
                    <PageTabs base={baseUrl}>
                        <TabLink to="/logs">Pipeline</TabLink>
                        <TabLink to="/changes">Changes</TabLink>
                        <TabLink to="/tests">Tests</TabLink>
                        <TabLink to="/artifacts">Artifacts</TabLink>
                    </PageTabs>
                    </div>
            </ModalHeader>
            <ModalBody>
                <div>
                    {React.cloneElement(this.props.children, { ...this.props })}
                </div>
            </ModalBody>
        </ModalView>);
    }
}

RunDetails.contextTypes = {
    config: object.isRequired,
    params: object,
    location: object,
    router: object.isRequired, // From react-router
};

RunDetails.propTypes = {
    children: PropTypes.node,
    runs: array,
    isMultiBranch: any,
    fetchIfNeeded: func,
    fetchRunsIfNeeded: func,
    setPipeline: func,
    getPipeline: func,
};

const selectors = createSelector(
    [runsSelector, isMultiBranchSelector],
    (runs, isMultiBranch) => ({ runs, isMultiBranch }));

export default connect(selectors, actions)(RunDetails);
