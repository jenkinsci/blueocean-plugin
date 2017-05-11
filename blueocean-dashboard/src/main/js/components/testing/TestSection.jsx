import React, { Component, PropTypes } from 'react';
import { observer } from 'mobx-react';
import { ShowMoreButton } from '@jenkins-cd/blueocean-core-js';
import TestCaseResultRow from './TestCaseResultRow';

@observer
export default class TestSection extends Component {

    propTypes = {
        titleKey: PropTypes.string,
        extraClasses: PropTypes.string,
        pager: PropTypes.object,
        t: PropTypes.func,
        locale: PropTypes.object,
        testService: PropTypes.object,
        total: PropTypes.number,
    };

    render() {
        const { t: translation, locale, pager, extraClasses, total, titleKey, testService } = this.props;
        const classes = `test-result-block ${extraClasses}`;
        let result = null;
        if (total > 0) {
            result = (
                <div className={classes}>
                    <h4>{translation(titleKey, { 0: total }) }</h4>
                    {pager.data.map((t, i) => <TestCaseResultRow key={i} testCase={t} translation={translation} locale={locale} testService={testService} />)}
                    <ShowMoreButton pager={pager} />
                </div>
            );
        }
        return result;
    }
}
