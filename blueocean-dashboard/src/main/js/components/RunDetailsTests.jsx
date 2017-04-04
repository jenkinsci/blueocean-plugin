import React, { Component, PropTypes } from 'react';
import { PlaceholderTable } from '@jenkins-cd/design-language';
import Extensions, { dataType } from '@jenkins-cd/js-extensions';

import { actions as selectorActions, testResults as testResultsSelector,
    connect, createSelector } from '../redux';
import Icon from './placeholder/Icon';
import { PlaceholderDialog } from './placeholder/PlaceholderDialog';


function NoTestsPlaceholder(props) {
    const { t } = props;

    const columns = [
        { width: 30, head: { text: 30 }, cell: { icon: 20 } },
        { width: 750, isFlexible: true, head: { text: 40 }, cell: { text: 200 } },
        { width: 80, head: {}, cell: { text: 50 } },
    ];

    const content = {
        icon: Icon.NOT_INTERESTED,
        title: t('rundetail.tests.results.empty.title'),
        linkText: t('rundetail.tests.results.empty.linktext'),
        linkHref: t('rundetail.tests.results.empty.linkhref'),
    };

    return (
        <div className="RunDetailsEmpty NoTests">
            <PlaceholderTable columns={columns} />
            <PlaceholderDialog width={300} content={content} />
        </div>
    );
}

NoTestsPlaceholder.propTypes = {
    t: PropTypes.func,
};


/**
 * Displays a list of tests from the supplied build run property.
 */
export class RunDetailsTests extends Component {
    componentWillMount() {
        this.props.fetchTestResults(
            this.props.result
        );
    }

    componentWillUnmount() {
        this.props.resetTestDetails();
    }

    render() {
        const { testResults, t, locale } = this.props;

        if (!testResults || testResults.$pending) {
            return null;
        }

        if (testResults.$failed) {
            return <NoTestsPlaceholder t={t} />;
        }

        return (
            <div className="test-results-container">
                <Extensions.Renderer
                  extensionPoint="jenkins.test.result"
                  filter={dataType(testResults)}
                  testResults={testResults}
                  locale={locale}
                  t={t}
                  run={this.props.result}
                />
            </div>
        );
    }
}

RunDetailsTests.propTypes = {
    params: PropTypes.object,
    isMultiBranch: PropTypes.bool,
    result: PropTypes.object,
    testResults: PropTypes.object,
    resetTestDetails: PropTypes.func,
    fetchTestResults: PropTypes.func,
    fetchTypeInfo: PropTypes.func,
    t: PropTypes.func,
    locale: PropTypes.string,
};

RunDetailsTests.contextTypes = {
    config: PropTypes.object.isRequired,
};

const selectors = createSelector([testResultsSelector],
    (testResults) => ({ testResults }));

export default connect(selectors, selectorActions)(RunDetailsTests);
