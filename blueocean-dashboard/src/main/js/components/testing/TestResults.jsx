import React, { Component, PropTypes } from 'react';
import { ResultItem, StatusIndicator, EmptyStateView } from '@jenkins-cd/design-language';
import moment from 'moment';

/* eslint-disable max-len */

const TestCaseResultRow = (props) => {
    const t = props.testCase;
    const duration = moment.duration(Number(t.duration), 'milliseconds').humanize();

    let testDetails = null;
    
    if (t.errorStackTrace) {
        testDetails = (
            <div className="test-details">
                <div className="test-detail-text" style={{ display: 'none' }}>
                    {duration}
                </div>
                <div className="test-console">
                    <h4>Error</h4>
                    <div className="error-message">
                        {t.errorDetails}
                    </div>
                    <h4>Output</h4>
                    <div className="stack-trace">
                        {t.errorStackTrace}
                    </div>
                </div>
            </div>
        );
    }
    
    let statusIndicator = null;
    switch (t.status) {
    case 'FAILED':
        statusIndicator = StatusIndicator.validResultValues.failure;
        break;
    case 'SKIPPED':
        statusIndicator = StatusIndicator.validResultValues.unstable;
        break;
    case 'FIXED':
    case 'PASSED':
        statusIndicator = StatusIndicator.validResultValues.success;
        break;
    default:
    }
    
    return (<ResultItem
      result={statusIndicator}
      expanded={false}
      label={`${t.name} - ${t.className}`}
      onExpand={null}
      extraInfo={duration}
    >
        { testDetails }
    </ResultItem>);
};

TestCaseResultRow.propTypes = {
    testCase: PropTypes.object,
};

export default class TestResult extends Component {

    render() {
        const testResults = this.props.testResults;
        const suites = this.props.testResults.suites;
        const tests = [].concat.apply([], suites.map(t => t.cases));
        
        // possible statuses: PASSED, FAILED, SKIPPED
        const failures = tests.filter(t => t.status === 'FAILED');
        const fixed = tests.filter(t => t.status === 'FIXED');
        const skipped = tests.filter(t => t.status === 'SKIPPED');
        const newFailures = failures.filter(t => t.age === 1);
        const existingFailures = failures.filter(t => t.age > 1);

        let passBlock = null;
        let newFailureBlock = null;
        let existingFailureBlock = null;
        let fixedBlock = null;
        let skippedBlock = null;
        let summaryBlock = null;
        
        if (testResults.failCount === 0) {
            passBlock = (
                <EmptyStateView iconName="done_all">
                    <h1 style={{ marginTop: '2.4rem' }}>All tests are passing</h1>
                    <p>Nice one! All {testResults.passCount} tests for this pipeline are passing.</p>
                    <p>How's the serenity?</p>
                </EmptyStateView>
            );
        } else {
            summaryBlock = (
                <div className="test-summary">
                    <div className={`new-passed count-${fixed.length}`}>
                        <div className="count">{fixed.length}</div>
                        <label>Fixed</label>
                    </div>
                    <div className={`new-failed count-${newFailures.length}`}>
                        <div className="count">{newFailures.length}</div>
                        <label>New Failures</label>
                    </div>
                    <div className={`failed count-${testResults.failCount}`}>
                        <div className="count">{testResults.failCount}</div>
                        <label>Failures</label>
                    </div>
                    <div className={`passed count-${testResults.passCount}`}>
                        <div className="count">{testResults.passCount}</div>
                        <label>Passing</label>
                    </div>
                    <div className={`skipped count-${testResults.skipCount}`}>
                        <div className="count">{testResults.skipCount}</div>
                        <label>Skipped</label>
                    </div>
                </div>
            );

            if (newFailures.length > 0) {
                newFailureBlock = [
                    <h4>New failing - {newFailures.length}</h4>,
                    newFailures.map((t, i) => <TestCaseResultRow key={i} testCase={t} />),
                ];
            }

            if (existingFailures.length > 0) {
                existingFailureBlock = [
                    <h4>Existing failures - {existingFailures.length}</h4>,
                    existingFailures.map((t, i) => <TestCaseResultRow key={i} testCase={t} />),
                ];
            }

            if (skipped.length > 0) {
                skippedBlock = [
                    <h4>Skipped - {skipped.length}</h4>,
                    skipped.map((t, i) => <TestCaseResultRow key={i} testCase={t} />),
                ];
            }
        }

        // always show fixed, whether showing totals or the encouraging message
        if (fixed.length > 0) {
            fixedBlock = (<div className="test-result-block fixed-block">
                <h4>Fixed</h4>
                {fixed.map((t, i) => <TestCaseResultRow key={i} testCase={t} />)}
            </div>);
        }

        return (
            <div>
                {passBlock}
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
    testResults: PropTypes.object,
};
