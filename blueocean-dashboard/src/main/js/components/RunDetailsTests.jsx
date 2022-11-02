import React, { Component, PropTypes } from 'react';
import { pagerService, i18nTranslator } from '@jenkins-cd/blueocean-core-js';

import TestResults from './testing/TestResults';
import TestService from './testing/TestService';
import NoTestsPlaceholder from './testing/NoTestsPlaceholder';
import { ComponentLink } from '../util/ComponentLink';

const t = i18nTranslator('blueocean-dashboard');

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
        testSummary: PropTypes.object,
    };

    contextTypes = {
        config: PropTypes.object.isRequired,
    };

    componentWillMount() {
        this.testService = new TestService(pagerService);
    }

    render() {
        const { locale, testSummary } = this.props;

        let result;
        if (testSummary && (testSummary.total || testSummary.total > 0)) {
            result = (
                <div className="test-results-container">
                    <TestResults
                        locale={locale}
                        t={t}
                        pipeline={this.props.pipeline}
                        run={this.props.result}
                        testService={this.testService}
                        testSummary={testSummary}
                    />
                </div>
            );
        } else {
            result = <NoTestsPlaceholder t={t} />;
        }
        return result;
    }
}

export default class RunDetailsTestsLink extends ComponentLink {
    name = 'tests';
    title = t('rundetail.header.tab.tests');
    component = RunDetailsTests;
    get notification() { return Math.min(99, (this.run.testSummary && parseInt(this.run.testSummary.failed)) || 0) || null; }
}
