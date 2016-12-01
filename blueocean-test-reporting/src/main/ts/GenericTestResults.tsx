import * as React from 'react';
const jdl = require('@jenkins-cd/design-language');
const { ResultItem, StatusIndicator, EmptyStateView } = jdl;
import * as moment from 'moment';

import {
    Extension,
    extensionPoints,
} from 'blueocean-js-extensions';

const {
    TestReportHandler,
} = extensionPoints;

@Extension(TestReportHandler)
class GenericTestReportHandler {
    isApplicable(runDetails) {
        return true;
    }
    getComponent() {
        return TestResult;
    }
}

/* eslint-disable max-len */

const ConsoleLog = ({ text, className, key = 'console' }) =>
    <div className={`${className} console-log insert-line-numbers`}>
        {text.trim().split('\n').map((line, idx) =>
            <div className="line" id={`#${key}-L${idx}`} key={`#${key}-L${idx}`}>{line}</div>
        )}
    </div>;

interface AnyTypes {
    testResults: any;
}

const TestCaseResultRow = (props) => {
    const t = props.testCase;
    const duration = moment.duration(Number(t.duration), 'milliseconds').humanize();

    let testDetails = null;

    if (t.errorStackTrace) {
        testDetails = (<div>
            <div className="test-details">
                <div className="test-detail-text" style={{ display: 'none' }}>
                    {duration}
                </div>
            </div>
            <div className="test-console">
                <h4>Error</h4>
                <ConsoleLog className="error-message" text={t.errorDetails} key={`${t}-message`} />
                <h4>Output</h4>
                <ConsoleLog className="stack-trace" text={t.errorStackTrace} key={`${t}-stack-trace`} />
            </div>
        </div>);
    }

    let statusIndicator = null;
    switch (t.status) {
    case 'REGRESSION':
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

export default class TestResult extends React.Component<AnyTypes, AnyTypes> {

    render() {
        const testResults = this.props.testResults;
        const suites = this.props.testResults.suites;
        const tests = [].concat.apply([], suites.map(t => t.cases));

        // one of 5 possible statuses: PASSED, FIXED, SKIPPED, FAILED, REGRESSION  see: hudson.tasks.junit.CaseResult$Status :(
        const fixed = tests.filter(t => t.status === 'FIXED');
        const skipped = tests.filter(t => t.status === 'SKIPPED');
        const newFailures = tests.filter(t => (t.age <= 1 && t.status === 'FAILED') || t.status === 'REGRESSION');
        const existingFailures = tests.filter(t => t.age > 1 && t.status === 'FAILED');

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
                newFailureBlock = (<div className="test-result-block new-failure-block">
                    <h4>New failing - {newFailures.length}</h4>
                    {newFailures.map((t, i) => <TestCaseResultRow key={i} testCase={t} />)}
                </div>);
            }

            if (existingFailures.length > 0) {
                existingFailureBlock = (<div className="test-result-block existing-failure-block">
                    <h4>Existing failures - {existingFailures.length}</h4>
                    {existingFailures.map((t, i) => <TestCaseResultRow key={i} testCase={t} />)}
                </div>);
            }

            if (skipped.length > 0) {
                skippedBlock = (<div className="test-result-block skipped-block">
                    <h4>Skipped - {skipped.length}</h4>
                    {skipped.map((t, i) => <TestCaseResultRow key={i} testCase={t} />)}
                </div>);
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
