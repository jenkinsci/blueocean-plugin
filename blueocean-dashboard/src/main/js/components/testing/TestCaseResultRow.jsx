import React, { Component, PropTypes } from 'react';
import { action, observable } from 'mobx';
import { observer } from 'mobx-react';
import { Fetch } from '@jenkins-cd/blueocean-core-js';
import { ResultItem, StatusIndicator } from '@jenkins-cd/design-language';
import moment from 'moment';
import TestDetails from './TestDetails';

/* eslint-disable max-len */

@observer
export default class TestCaseResultRow extends Component {

    propTypes = {
        testCase: PropTypes.object,
        translation: PropTypes.func,
        locale: PropTypes.string,
    };

    componentWillMount() {
        this.setState({ isFocused: false, attemptedFetchForStdOutStdErr: false });
    }

    @action
    setStdout(stdout) {
        this.stdout = stdout;
    }

    @action
    setStderr(stderr) {
        this.stderr = stderr;
    }

    @observable stdout = null;
    @observable stderr = null;

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
        case 'SKIPPED':
            statusIndicator = StatusIndicator.validResultValues.not_built;
            break;
        case 'FIXED':
            statusIndicator = StatusIndicator.validResultValues.success;
            break;
        default:
        }

        const onExpand = () => {
            this.setState({ isFocused: true, attemptedFetchForStdOutStdErr: true });
            // 404s are valid responses for stderr/stdout
            const ignoreNotFound = (e) => {
                if (e.response.status !== 404) {
                    throw e;
                }
            };
            // Do not attempt to fetch if we've already done this
            if (this.state.attemptedFetchForStdOutStdErr) {
                return;
            }
            if (!t._links) {
                return;
            }
            Fetch.fetch(t._links.stdout.href)
                .then(res => res.text())
                .then(data => this.setStdout(data))
                .catch(ignoreNotFound);
            Fetch.fetch(t._links.stderr.href)
                .then(res => res.text())
                .then(data => this.setStderr(data))
                .catch(ignoreNotFound);
        };
        const onCollapse = () => {
            this.setState({ isFocused: false });
        };
        const testDetails = showTestCase ?
            <TestDetails test={ t } duration={ duration } stdout={ this.stdout } stderr={ this.stderr } translation={ translation } /> : null;
        return (<ResultItem
            result={ statusIndicator }
            expanded={ this.state.isFocused }
            label={ `${t.name}` }
            onExpand={ onExpand }
            onCollapse={ onCollapse }
            extraInfo={ duration }
        >
            { testDetails }
        </ResultItem>);
    }
}
