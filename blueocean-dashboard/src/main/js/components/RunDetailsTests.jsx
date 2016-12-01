import React, { Component, PropTypes } from 'react';
import { EmptyStateView } from '@jenkins-cd/design-language';
import Extensions, { dataType } from '@jenkins-cd/js-extensions';
import Markdown from 'react-remarkable';
import { actions as selectorActions, testResults as testResultsSelector,
    connect, createSelector } from '../redux';
import PageLoading from './PageLoading';

import {
    ExtensionPoint,
    Extension,
    ExtensionList,
    ExtensionRenderer,
    Ordinal,
    extensionPoints,
} from 'blueocean-js-extensions';

const {
    RunDetailsLink,
} = extensionPoints;

@ExtensionPoint
class TestReportHandler {
    isApplicable(runDetails) {
        return true;
    }
    getComponent() {
        return undefined;
    }
}

@Extension
export class TestReportLink extends RunDetailsLink {
    @ExtensionList(TestReportHandler) testReportHandlers;
    
    isApplicable(runDetails) {
        for (let h of this.testReportHandlers) {
            if (h.isApplicable(runDetails)) {
                return true;
            }
        }
        return false;
    }
    
    name() {
        return 'Tests';
    }
    
    url() {
        return '/tests';
    }
    getComponent() {
        for (let h of this.testReportHandlers) {
            if (h.isApplicable(runDetails)) {
                return h.getComponent();
            }
        }
    }
}

/**
 * Displays a list of tests from the supplied build run property.
 */
export class RunDetailsTests extends Component {
    @ExtensionList(TestReportHandler) testReportHandlers;
    
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
            return <PageLoading />;
        }

        if (testResults.$failed) {
            return (<EmptyStateView tightSpacing>
                 <Markdown>
                    {t('EmptyState.tests', {
                        defaultValue: 'There are no tests run for this build.\n\n',
                    })}
                </Markdown>
            </EmptyStateView>);
        }

        const percentComplete = testResults.passCount /
            (testResults.passCount + testResults.failCount);

        return (<div className="test-results-container">
            <div className="test=result-summary" style={{ display: 'none' }}>
                <div className={`test-result-bar ${percentComplete}%`}></div>
                <div className="test-result-passed">{t('rundetail.tests.passed', {
                    0: testResults.passCount,
                    defaultValue: 'Passed {0}',
                })}</div>
                <div className="test-result-failed">{t('rundetail.tests.failed', {
                    0: testResults.failCount,
                    defaultValue: 'Failed {0}',
                })}</div>
                <div className="test-result-skipped">{t('rundetail.tests.skipped', {
                    0: testResults.skipCount,
                    defaultValue: 'Skipped {0}',
                })}</div>
                <div className="test-result-duration">{t('rundetail.tests.duration', {
                    0: testResults.duration,
                    defaultValue: 'Duration {0}',
                })}</div>
            </div>
            
            {/* we should decorate data coming back from the JSON APIs instead of this
                to emulate the way actions work. By using classes as extension
                points, we can define whichever methods we need to obtain different
                views as react components */}
            {this.testReportHandlers.map(h => {
                let component = h.getComponent();
                return <ExtensionRenderer extension={component} testResults={testResults} />;
            })}

            {/*
            <Extensions.Renderer
              extensionPoint="jenkins.test.result"
              filter={dataType(testResults)}
              testResults={testResults}
              locale={locale}
              t={t}
            />
            */}
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
    t: PropTypes.func,
    locale: PropTypes.string,
};

RunDetailsTests.contextTypes = {
    config: PropTypes.object.isRequired,
};

const selectors = createSelector([testResultsSelector],
    (testResults) => ({ testResults }));

export default connect(selectors, selectorActions)(RunDetailsTests);
