import React, { Component, PropTypes } from 'react';
import { observer } from 'mobx-react';
import { pagerService } from '@jenkins-cd/blueocean-core-js';
import { TestSummary } from './TestSummary';
import TestService from './TestService';
import TestSection from './TestSection';
// needs to be loaded since the moment lib will use require which in run time will fail
import 'moment/min/locales.min';

/* eslint-disable max-len */

@observer
export default class TestResults extends Component {

    propTypes = {
        pipeline: PropTypes.object,
        run: PropTypes.object,
        t: PropTypes.func,
        locale: PropTypes.string,
    };

    componentWillMount() {
        this.testService = new TestService(pagerService);
        this._initPagers(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._initPagers(nextProps);
    }

    _initPagers(props) {
        const pipeline = props.pipeline;
        const run = props.run;
        this.regressionsPager = this.testService.newRegressionsPager(pipeline, run);
        this.existingFailedPager = this.testService.newExistingFailedPager(pipeline, run);
        this.skippedPager = this.testService.newSkippedPager(pipeline, run);
        this.fixedPager = this.testService.newFixedPager(pipeline, run);
    }

    render() {
        const { t: translation, locale, run } = this.props;
        return (
            <div>
                <TestSummary
                    translate={translation}
                    passing={run.testSummary.passed}
                    fixed={run.testSummary.fixed}
                    failuresNew={run.testSummary.regressions}
                    failuresExisting={run.testSummary.existingFailed}
                    skipped={run.testSummary.skipped}
                />
                <TestSection
                    titleKey="rundetail.tests.results.errors.new.count"
                    pager={this.regressionsPager}
                    extraClasses="new-failure-block"
                    locale={locale} t={translation}
                    total={run.testSummary.regressions}
                    testService={this.testService}
                />
                <TestSection
                    titleKey="rundetail.tests.results.errors.existing.count"
                    pager={this.existingFailedPager}
                    extraClasses="existing-failure-block"
                    locale={locale} t={translation}
                    total={run.testSummary.existingFailed}
                    testService={this.testService}
                />
                <TestSection
                    titleKey="rundetail.tests.results.fixed"
                    pager={this.fixedPager}
                    extraClasses="fixed-block"
                    locale={locale} t={translation}
                    total={run.testSummary.fixed}
                    testService={this.testService}
                />
                <TestSection
                    titleKey="rundetail.tests.results.skipped.count"
                    pager={this.skippedPager}
                    extraClasses="skipped-block"
                    locale={locale} t={translation}
                    total={run.testSummary.skipped}
                    testService={this.testService}
                />
            </div>
        );
    }
}
