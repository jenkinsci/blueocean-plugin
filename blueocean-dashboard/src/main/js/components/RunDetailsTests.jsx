import React, { Component, PropTypes } from 'react';
import { pagerService } from '@jenkins-cd/blueocean-core-js';

import TestResults from './testing/TestResults';
import TestService from './testing/TestService';
import NoTestsPlaceholder from './testing/NoTestsPlaceholder';

const t = require('@jenkins-cd/blueocean-core-js').i18nTranslator('blueocean-dashboard');

/**
 * Displays a list of tests from the supplied build run property.
 */
export class RunDetailsTests extends Component {

    propTypes = {
        params: PropTypes.object,
        pipeline: PropTypes.object,
        isMultiBranch: PropTypes.bool,
        result: PropTypes.object,
        fetchTypeInfo: PropTypes.func,
        locale: PropTypes.string,
    };

    contextTypes = {
        config: PropTypes.object.isRequired,
    };

    componentWillMount() {
        this.testService = new TestService(pagerService);
    }

    render() {
        const { locale } = this.props;

        let result;
        if (this.props.result.testSummary.total || this.props.result.testSummary.total > 0) {
            result = (
                <div className="test-results-container">
                    <TestResults
                        locale={locale}
                        t={t}
                        pipeline={this.props.pipeline}
                        run={this.props.result}
                        testService={this.testService}
                    />
                </div>
            );
        } else {
            result = (<NoTestsPlaceholder t={this.props.t} />);
        }
        return result;
    }
}

export default {
    name: "tests",
    title: t('rundetail.header.tab.tests'),
    component: RunDetailsTests,
    getBadgeText: run => Math.min(99, run.testSummary && parseInt(run.testSummary.failed) || 0) || null,
};
