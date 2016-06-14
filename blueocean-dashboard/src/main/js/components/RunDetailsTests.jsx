import React, { Component, PropTypes } from 'react';
import { EmptyStateView } from '@jenkins-cd/design-language';
import { actions, testResults as testResultsSelector, connect, createSelector } from '../redux';

const { object, func } = PropTypes;

/**
 * Displays a list of tests from the supplied build run property.
 */
export default class RunDetailsTests extends Component {
    renderEmptyState() {
        return (
            <EmptyStateView tightSpacing>
                <p>There are no tests for this pipeline run.</p>
            </EmptyStateView>
        );
    }

    componentWillMount() {
        this.props.fetchTestResults(
            this.context.config, 
            {
                isMultiBranch: this.props.isMultiBranch,
                organization: this.props.params.organization,
                pipeline: this.props.params.pipeline,
                branch: this.props.params.branch,
                runId: this.props.params.runId,
            }
        );
    }

    render() {
        const { result, testResults } = this.props;
        
        if (!testResults || !testResults.suites) {
            return null;
        }

        // TODO: Move the rest of this into a plugin.
        // passing it testResults via an ExtensionPoint ? Hmmm
        const suites = testResults.suites;
        return (<div>
            {
                suites.map((suite) => 
                    <div>{suite.name}</div>    
                )
            }
        </div>);
    }
}

RunDetailsTests.propTypes = {
    result: object,
    testResults: object,
    fetchTestResults: func,
};

const selectors = createSelector([testResultsSelector], (testResults) => ({ testResults }));

export default connect(selectors, actions)(RunDetailsTests);
