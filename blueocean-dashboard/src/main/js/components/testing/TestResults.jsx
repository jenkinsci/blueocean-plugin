import React, {Component, PropTypes} from "react";
import {EmptyStateView, ResultItem, StatusIndicator} from "@jenkins-cd/design-language";
import {TestSummary} from "./TestSummary";
// needs to be loaded since the moment lib will use require which in run time will fail
import "moment/min/locales.min";
import TestCaseResultRow from "./TestCaseResultRow";

/* eslint-disable max-len */

export default class TestResults extends Component {

    render() {
        const { t: translation, tests, locale, run } = this.props;

        const fixed = tests.filter(t => t.state === 'FIXED');
        const skipped = tests.filter(t => t.status === 'SKIPPED');
        const newFailures = tests.filter(t => (t.age <= 1 && t.status === 'FAILED') || t.state === 'REGRESSION');
        const existingFailures = tests.filter(t => t.age > 1 && t.status === 'FAILED');

        let newFailureBlock = null;
        let existingFailureBlock = null;
        let fixedBlock = null;
        let skippedBlock = null;

        const summaryBlock = (
            <TestSummary
                translate={translation}
                passing={run.testSummary.passed}
                fixed={run.testSummary.fixed}
                failuresNew={run.testSummary.regressions}
                failuresExisting={run.testSummary.existingFailed}
                skipped={run.testSummary.skipped}
            />
        );

        if (newFailures.length > 0) {
            newFailureBlock = (<div className="test-result-block new-failure-block">
                <h4>{translation('rundetail.testResults.results.errors.new.count', {
                    0: run.testSummary.regressions,
                    defaultValue: 'New failing - {0}',
                })}</h4>
                {newFailures.map((t, i) => <TestCaseResultRow key={i} testCase={t} translation={translation} locale={locale} />)}
            </div>);
        }

        if (existingFailures.length > 0) {
            existingFailureBlock = (<div className="test-result-block existing-failure-block">
                <h4>{translation('rundetail.testResults.results.errors.existing.count', {
                    0: run.testSummary.existingFailed,
                    defaultValue: 'Existing failures - {0}',
                })}</h4>
                {existingFailures.map((t, i) => <TestCaseResultRow key={i} testCase={t} translation={translation} locale={locale} />)}
            </div>);
        }

        if (skipped.length > 0) {
            skippedBlock = (<div className="test-result-block skipped-block">
                <h4>{translation('rundetail.testResults.results.skipped.count', {
                    0: run.testSummary.skipped,
                    defaultValue: 'Skipped - {0}',
                })}</h4>
                {skipped.map((t, i) => <TestCaseResultRow key={i} testCase={t} translation={translation} locale={locale} />)}
            </div>);
        }

        // always show fixed, whether showing totals or the encouraging message
        if (fixed.length > 0) {
            fixedBlock = (<div className="test-result-block fixed-block">
                <h4>{translation('rundetail.testResults.results.fixed', {
                    0: run.testSummary.fixed,
                    defaultValue: 'Fixed – {0}',
                })}</h4>
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

TestResults.propTypes = {
    run: PropTypes.object,
    tests: PropTypes.object,
    t: PropTypes.func,
    locale: PropTypes.string,
};
