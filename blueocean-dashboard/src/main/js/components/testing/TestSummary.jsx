import React, { PropTypes } from 'react';
import Icon from '../placeholder/Icon';

export function TestSummary(props) {
    const { translate, passing, fixed, failuresNew, failuresExisting, skipped } = props;
    const failuresTotal = failuresNew + failuresExisting;
    const extraClass = failuresTotal ? 'u-failed' : '';

    const icon = React.cloneElement(Icon.PULL_REQUEST, { className: 'icon' });
    let title = '';
    let message = '';

    if (failuresTotal === 0 && fixed === 0) {
        title = translate('rundetail.tests.results.summary.passing_title');
        message = translate('rundetail.tests.results.summary.passing_message', { 0: passing });
    } else if (failuresTotal === 0 && fixed > 0) {
        title = translate('rundetail.tests.results.summary.passing_after_fixes_title');
        message = translate('rundetail.tests.results.summary.passing_after_fixes_message', { 0: fixed, 1: passing });
    } else if (failuresTotal > 0) {
        title = translate('rundetail.tests.results.summary.failing_title', { 0: failuresTotal });
        message = translate('rundetail.tests.results.summary.failing_message', { 0: failuresNew, 1: failuresExisting });
    }

    return (
        <div className={`TestSummary ${extraClass}`}>
            {icon}

            <div className="content">
                <h1>{title}</h1>

                <p>{message}</p>
            </div>
        </div>
    );
}

TestSummary.propTypes = {
    translate: PropTypes.func,
    passing: PropTypes.number,
    fixed: PropTypes.number,
    failuresNew: PropTypes.number,
    failuresExisting: PropTypes.number,
    skipped: PropTypes.number,
};
