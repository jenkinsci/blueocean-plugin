import React, { Component, PropTypes } from 'react';
import { EmptyStateView } from '@jenkins-cd/design-language';
import Extensions, { dataType } from '@jenkins-cd/js-extensions';
import Markdown from 'react-remarkable';
import { actions as selectorActions, testResults as testResultsSelector,
    connect, createSelector } from '../redux';

/**
 * Displays a list of tests from the supplied build run property.
 */
export class RunDetailsTests extends Component {
    componentWillMount() {
        this.props.fetchTestResults(
            this.props.result
        );
    }

    componentWillUnmount() {
        this.props.resetTestDetails();
    }

    render() {
        const { testResults, t, locale } = this.props;

        if (!testResults || testResults.$pending) {
            return null;
        }

        if (testResults.$failed) {
            return (<EmptyStateView tightSpacing>
                 <Markdown>
                    {t('EmptyState.tests', {
                        defaultValue: 'There are no tests archived for this run.\n\n',
                    })}
                </Markdown>
            </EmptyStateView>);
        }

        return (
            <div className="test-results-container">
                <Extensions.Renderer
                  extensionPoint="jenkins.test.result"
                  filter={dataType(testResults)}
                  testResults={testResults}
                  locale={locale}
                  t={t}
                  run={this.props.result}
                />
            </div>
        );
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
    t: PropTypes.func,
    locale: PropTypes.string,
};

RunDetailsTests.contextTypes = {
    config: PropTypes.object.isRequired,
};

const selectors = createSelector([testResultsSelector],
    (testResults) => ({ testResults }));

export default connect(selectors, selectorActions)(RunDetailsTests);
