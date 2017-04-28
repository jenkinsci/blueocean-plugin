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
        this.setState({ isFocused: false, attemptedFetchForStdOutStdErr: false });
        this.logService = this.props.testService.testLogs();
    }

    render() {
        console.log('row render');
        const { testCase: t, translation, locale = 'en', testService } = this.props;
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
            this.logService.loadStdOut(t);
            this.logService.loadStdErr(t);
        };
        const onCollapse = () => {
            this.setState({ isFocused: false });
        };

        const stdout = this.logService.getStdOut(t);
        const stderr = testService.testLogs().getStdErr(t);

        const testDetails = showTestCase ?
            <TestDetails test={ t } duration={ duration } stdout={ stdout } stderr={ stderr } translation={ translation } /> : null;
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
