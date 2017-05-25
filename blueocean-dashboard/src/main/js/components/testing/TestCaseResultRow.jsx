import React, { Component, PropTypes } from 'react';
import { observer } from 'mobx-react';
import { ResultItem, StatusIndicator } from '@jenkins-cd/design-language';
import moment from 'moment';
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
        const { testCase: t, translation, locale = 'en' } = this.props;
        moment.locale(locale);
        const duration = moment.duration(Number(t.duration), 'milliseconds').humanize();
        const showTestCase = (t.errorStackTrace || t.errorDetails || this.stdout || this.stderr);
        let statusIndicator = null;
        switch (t.status) {
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
            if (!t._links) {
                return;
            }
            if (!this.stdout) {
                this.logService.loadStdOut(t);
            }
            if (!this.stderr) {
                this.logService.loadStdErr(t);
            }
        };
        const onCollapse = () => {
            this.setState({ isFocused: false });
        };

        this.stdout = this.logService.getStdOut(t);
        this.stderr = this.logService.getStdErr(t);

        const testDetails = showTestCase ?
            <TestDetails
                test={ t }
                duration={ duration }
                stdout={ this.stdout && this.stdout.log ? this.stdout.log : null }
                stderr={ this.stderr && this.stderr.log ? this.stderr.log : null }
                translation={ translation }
            /> : null;
        return (
            <ResultItem
                result={ statusIndicator }
                expanded={ this.state.isFocused }
                label={ `${t.name}` }
                onExpand={ onExpand }
                onCollapse={ onCollapse }
                extraInfo={ duration }
            >
            { testDetails }
            </ResultItem>
        );
    }
}
