import React, { Component, PropTypes } from 'react';
import { EmptyStateView } from '@jenkins-cd/design-language';

const { object } = PropTypes;

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

    render() {
        const { result } = this.props;

        if (!result) {
            return null;
        }

        // TODO: impl logic to display table of data
        return this.renderEmptyState();
    }
}

RunDetailsTests.propTypes = {
    result: object,
};
