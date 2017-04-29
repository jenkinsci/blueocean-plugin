import React, { Component, PropTypes } from 'react';
import { pagerService } from '@jenkins-cd/blueocean-core-js';

import TestResults from './testing/TestResults';
import TestService from './testing/TestService';
import NoTestsPlaceholder from './testing/NoTestsPlaceholder';

/**
 * Displays a list of tests from the supplied build run property.
 */
export default class RunDetailsTests extends Component {

    propTypes = {
        params: PropTypes.object,
        pipeline: PropTypes.object,
        isMultiBranch: PropTypes.bool,
        result: PropTypes.object,
        fetchTypeInfo: PropTypes.func,
        t: PropTypes.func,
        locale: PropTypes.string,
    };

    contextTypes = {
        config: PropTypes.object.isRequired,
    };

    componentWillMount() {
        this.testService = new TestService(pagerService);
    }

    render() {
        const { t, locale } = this.props;

        let result;
        if (this.props.pipeline.testSummary) {
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

