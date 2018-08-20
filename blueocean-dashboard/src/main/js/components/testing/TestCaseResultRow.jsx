import React, { Component, PropTypes } from 'react';
import { observer } from 'mobx-react';
import { ResultItem, StatusIndicator, TimeDuration } from '@jenkins-cd/design-language';
import TestDetails from './TestDetails';

@observer
export default class TestCaseResultRow extends Component {
    propTypes = {
        testService: PropTypes.object,
        testCase: PropTypes.object,
        translation: PropTypes.func,
        locale: PropTypes.string,
    };

    componentWillMount() {
        this.stdout = null;
        this.stderr = null;
        this.setState({ isFocused: false, attemptedFetchForStdOutStdErr: false });
        this.logService = this.props.testService.testLogs();
    }

    render() {
        const { testCase, translation, locale = 'en' } = this.props;
        const duration = TimeDuration.format(testCase.duration * 1000, translation, locale);
        const showTestCase = testCase.errorStackTrace || testCase.errorDetails || testCase.hasStdLog;
        let statusIndicator = null;
        switch (testCase.status) {
            case 'FAILED':
                statusIndicator = StatusIndicator.validResultValues.failure;
                break;
            case 'PASSED':
                statusIndicator = StatusIndicator.validResultValues.success;
                break;
            case 'SKIPPED':
                statusIndicator = StatusIndicator.validResultValues.not_built;
                break;
            default:
                statusIndicator = StatusIndicator.validResultValues.unknown;
        }

        const onExpand = () => {
            this.setState({ isFocused: true, attemptedFetchForStdOutStdErr: true });
            if (!testCase._links) {
                return;
            }
            if (!this.stdout) {
                this.logService.loadStdOut(testCase);
            }
            if (!this.stderr) {
                this.logService.loadStdErr(testCase);
            }
        };

        const onCollapse = () => {
            this.setState({ isFocused: false });
        };

        this.stdout = this.logService.getStdOut(testCase);
        this.stderr = this.logService.getStdErr(testCase);

        const testDetails = showTestCase ? (
            <TestDetails
                test={testCase}
                duration={duration}
                stdout={this.stdout && this.stdout.log ? this.stdout.log : null}
                stderr={this.stderr && this.stderr.log ? this.stderr.log : null}
                translation={translation}
            />
        ) : null;
        return (
            <ResultItem
                result={statusIndicator}
                expanded={this.state.isFocused}
                label={`${testCase.name}`}
                onExpand={onExpand}
                onCollapse={onCollapse}
                extraInfo={duration}
            >
                {testDetails}
            </ResultItem>
        );
    }
}
