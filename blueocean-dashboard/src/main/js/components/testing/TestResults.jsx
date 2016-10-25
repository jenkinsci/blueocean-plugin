import React, { Component, PropTypes } from 'react';
import { ResultItem, StatusIndicator, EmptyStateView } from '@jenkins-cd/design-language';
import moment from 'moment';
// needs to be loaded since the moment lib will use require which in run time will fail
import 'moment/min/locales.min';

/* eslint-disable max-len */

const ConsoleLog = ({ text, className, key = 'console' }) =>
    <div className={`${className} console-log insert-line-numbers`}>
        {text.trim().split('\n').map((line, idx) =>
            <div className="line" id={`#${key}-L${idx}`} key={`#${key}-L${idx}`}>{line}</div>
        )}
    </div>;

ConsoleLog.propTypes = {
    text: PropTypes.string,
    className: PropTypes.string,
    key: PropTypes.string,
};

const TestCaseResultRow = (props) => {
    const { testCase: t, translation, locale = 'en' } = props;
    moment.locale(locale);
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
                <h4>{translation('Error')}</h4>
                <ConsoleLog className="error-message" text={t.errorDetails} key={`${t}-message`} />
                <h4>{translation('Output')}</h4>
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

TestCaseResultRow.propTypes = {
    testCase: PropTypes.object,
    translation: PropTypes.func,
    locale: PropTypes.string,
};

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

        let passBlock = null;
        let newFailureBlock = null;
        let existingFailureBlock = null;
        let fixedBlock = null;
        let skippedBlock = null;
        let summaryBlock = null;
        
        if (testResults.failCount === 0) {
            passBlock = (
                <EmptyStateView iconName="done_all">
                    <h1 style={{ marginTop: '2.4rem' }}>{translation('Tests.passing')}</h1>
                    <p>{translation('Tests.passing.count', { 0: testResults.passCount })}</p>
                    <p>{translation('Serenity')}</p>
                </EmptyStateView>
            );
        } else {
            summaryBlock = (
                <div className="test-summary">
                    <div className={`new-passed count-${fixed.length}`}>
                        <div className="count">{fixed.length}</div>
                        <label>{translation('Fixed')}</label>
                    </div>
                    <div className={`new-failed count-${newFailures.length}`}>
                        <div className="count">{newFailures.length}</div>
                        <label>{translation('New.failures')}</label>
                    </div>
                    <div className={`failed count-${testResults.failCount}`}>
                        <div className="count">{testResults.failCount}</div>
                        <label>{translation('Failures')}</label>
                    </div>
                    <div className={`passed count-${testResults.passCount}`}>
                        <div className="count">{testResults.passCount}</div>
                        <label>{translation('Passing.singular')}</label>
                    </div>
                    <div className={`skipped count-${testResults.skipCount}`}>
                        <div className="count">{testResults.skipCount}</div>
                        <label>{translation('Skipped.singular')}</label>
                    </div>
                </div>
            );

            if (newFailures.length > 0) {
                newFailureBlock = (<div className="test-result-block new-failure-block">
                    <h4>{translation('New.error', { 0: newFailures.length })}</h4>
                    {newFailures.map((t, i) => <TestCaseResultRow key={i} testCase={t} translation={translation} locale={locale} />)}
                </div>);
            }

            if (existingFailures.length > 0) {
                existingFailureBlock = (<div className="test-result-block existing-failure-block">
                    <h4>{translation('Existing.error', { 0: existingFailures.length })}</h4>
                    {existingFailures.map((t, i) => <TestCaseResultRow key={i} testCase={t} translation={translation} locale={locale} />)}
                </div>);
            }

            if (skipped.length > 0) {
                skippedBlock = (<div className="test-result-block skipped-block">
                    <h4>{translation('Skipped', { 0: skipped.length })}</h4>
                    {skipped.map((t, i) => <TestCaseResultRow key={i} testCase={t} translation={translation} locale={locale} />)}
                </div>);
            }
        }

        // always show fixed, whether showing totals or the encouraging message
        if (fixed.length > 0) {
            fixedBlock = (<div className="test-result-block fixed-block">
                <h4>{translation('Fixed')}</h4>
                {fixed.map((t, i) => <TestCaseResultRow key={i} testCase={t} translation={translation} locale={locale} />)}
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
    t: PropTypes.func,
    locale: PropTypes.string,
};
