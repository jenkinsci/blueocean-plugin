import React, { Component, PropTypes } from 'react';
import { EmptyStateView } from '@jenkins-cd/design-language';
import { actions as selectorActions, testResults as testResultsSelector,
    connect, createSelector } from '../redux';
import Extensions from '@jenkins-cd/js-extensions';

const EmptyState = () => (
    <EmptyStateView tightSpacing>
        <p>
            There are no tests run for this build.
        </p>
    </EmptyStateView>
);


/**
 * Displays a list of tests from the supplied build run property.
 */
export class RunDetailsTests extends Component {
    componentWillMount() {
        if (this.context.config) {
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
    }
    
    componentWillUnmount() {
        this.props.resetTestDetails();
    }

    renderEmptyState() {
        return (
            <EmptyStateView tightSpacing>
                <p>There are no tests for this pipeline run.</p>
            </EmptyStateView>
        );
    }

    render() {
        const { testResults } = this.props;
        
        if (!testResults) {
            return null;
        }
        
        if (!testResults.suites) {
            return <EmptyState />;
        }
        
        const percentComplete = testResults.passCount /
            (testResults.passCount + testResults.failCount);
        
        return (<div className="test-results-container">
            <div className="test=result-summary" style={{ display: 'none' }}>
                <div className={`test-result-bar ${percentComplete}%`}></div>
                <div className="test-result-passed">Passed {testResults.passCount}</div>
                <div className="test-result-failed">Failed {testResults.failCount}</div>
                <div className="test-result-skipped">Skipped {testResults.skipCount}</div>
                <div className="test-result-duration">Duration {testResults.duration}</div>
            </div>
            
            <Extensions.Renderer extensionPoint="jenkins.test.result" dataType={testResults} testResults={testResults} />
        </div>);
    }
}

RunDetailsTests.propTypes = {
    params: PropTypes.object,
    isMultiBranch: PropTypes.bool,
    result: PropTypes.object,
    testResults: PropTypes.object,
    resetTestDetails: PropTypes.func,
    fetchTestResults: PropTypes.func,
    fetchTypeInfo: PropTypes.func,
};

RunDetailsTests.contextTypes = {
    config: PropTypes.object.isRequired,
};

const selectors = createSelector([testResultsSelector],
    (testResults) => ({ testResults }));

export default connect(selectors, selectorActions)(RunDetailsTests);
