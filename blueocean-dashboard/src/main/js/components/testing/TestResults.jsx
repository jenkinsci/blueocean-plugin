import React, { Component, PropTypes } from 'react';
import { ResultItem, StatusIndicator } from '@jenkins-cd/design-language';
import { TestSummary } from './TestSummary';
import { EmptyStateView } from '@jenkins-cd/design-language';
// needs to be loaded since the moment lib will use require which in run time will fail
import 'moment/min/locales.min';
import TestCaseResultRow from './TestCaseResultRow';

/* eslint-disable max-len */

export default class TestResult extends Component {

    render() {
        const { t: translation, testResults, locale } = this.props;
        const suites = this.props.testResults.suites;
        const tests = [].concat.apply([], suites.map(t => t.cases));

        // one of 5 possible statuses: PASSED, FIXED, SKIPPED, FAILED, REGRESSION  see: hudson.tasks.junit.CaseResult$Status :(
        const fixed = tests.filter(t => t.status === 'FIXED');
        const skipped = tests.filter(t => t.status === 'SKIPPED');
        const newFailures = tests.filter(t => (t.age <= 1 && t.status === 'FAILED') || t.status === 'REGRESSION');
        const existingFailures = tests.filter(t => t.age > 1 && t.status === 'FAILED');

        let newFailureBlock = null;
        let existingFailureBlock = null;
        let fixedBlock = null;
        let skippedBlock = null;

        const summaryBlock = (
            <TestSummary
                translate={translation}
                passing={testResults.passCount}
                fixed={fixed.length}
                failuresNew={newFailures.length}
                failuresExisting={existingFailures.length}
                skipped={testResults.skipCount}
            />
        );

        if (newFailures.length > 0) {
            newFailureBlock = (<div className="test-result-block new-failure-block">
                <h4>{translation('rundetail.tests.results.errors.new.count', {
                    0: newFailures.length,
                    defaultValue: 'New failing - {0}',
                })}</h4>
                {newFailures.map((t, i) => <TestCaseResultRow key={i} testCase={t} translation={translation} locale={locale} />)}
            </div>);
        }

        if (existingFailures.length > 0) {
            existingFailureBlock = (<div className="test-result-block existing-failure-block">
                <h4>{translation('rundetail.tests.results.errors.existing.count', {
                    0: existingFailures.length,
                    defaultValue: 'Existing failures - {0}',
                })}</h4>
                {existingFailures.map((t, i) => <TestCaseResultRow key={i} testCase={t} translation={translation} locale={locale} />)}
            </div>);
        }

        if (skipped.length > 0) {
            skippedBlock = (<div className="test-result-block skipped-block">
                <h4>{translation('rundetail.tests.results.skipped.count', {
                    0: skipped.length,
                    defaultValue: 'Skipped - {0}',
                })}</h4>
                {skipped.map((t, i) => <TestCaseResultRow key={i} testCase={t} translation={translation} locale={locale} />)}
            </div>);
        }

        // always show fixed, whether showing totals or the encouraging message
        if (fixed.length > 0) {
            fixedBlock = (<div className="test-result-block fixed-block">
                <h4>{translation('rundetail.tests.results.fixed', { defaultValue: 'Fixed' })}</h4>
                {fixed.map((t, i) => <TestCaseResultRow key={i} testCase={t} translation={translation} locale={locale} />)}
            </div>);
        }

        return (
            <div>
                {summaryBlock}
                {newFailureBlock}
                {existingFailureBlock}
                {fixedBlock}
                {skippedBlock}
            </div>
        );
    }
}

TestResult.propTypes = {
    run: PropTypes.object,
    testResults: PropTypes.object,
    t: PropTypes.func,
    locale: PropTypes.string,
};
