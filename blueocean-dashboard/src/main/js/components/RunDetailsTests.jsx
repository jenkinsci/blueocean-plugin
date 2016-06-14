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
        console.log(this.props);
        this.props.fetchTestResults();
    }

    render() {
        const { result, testResults } = this.props;

        console.log(testResults);
        
        if (!testResults) {
            return null;
        }

        return (<div>
            [{testResults.message}]
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
