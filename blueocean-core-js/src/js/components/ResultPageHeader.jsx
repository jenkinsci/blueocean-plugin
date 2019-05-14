import React, { PropTypes } from 'react';
import { BasicHeader, TopNav, HeaderDetails, LiveStatusIndicator } from '@jenkins-cd/design-language';

import { _wrap } from './ContentPageHeader';

// Exported from Zeplin, path could use a little cleanup
const CloseButton = props => (
    <span title={props.t('creation.core.header.close', { defaultValue: 'Close' })}>
        <svg className="ResultPageHeader-close" width="24px" height="24px" viewBox="0 0 24 24" onClick={props.onClick} version="1.1">
            <g strokeWidth="1">
                <path strokeWidth="2" d="M18,10l-6,-6l-6,6m6,-6v16" fill="none" stroke="white"/>
            </g>
        </svg>
    </span>
);

CloseButton.propTypes = {
    onClick: PropTypes.func,
    t: PropTypes.func,
};

export const ResultPageHeader = props => {
    const { status = 'unknown', title, onCloseClick, startTime, estimatedDurationInMillis, t } = props;

    const closeClicked = () => {
        if (onCloseClick) {
            onCloseClick();
        }
    };

    const titleComp = _wrap(title, 'div', { className: 'ResultPageHeader-title u-flex-grow' });
    const topNavLinks = _wrap(props.topNavLinks, 'nav');
    const runButton = _wrap(props.runButton, 'div', { className: 'ResultPageHeader-run' });

    const classNames = ['ResultPageHeader'];
    if (props.className) {
        classNames.push(props.className);
    }

    return (
        <BasicHeader className={classNames.join(' ')} statusColor={status}>
            <TopNav>
                <section className="ResultPageHeader-indicator status inverse">
                    <LiveStatusIndicator result={status} startTime={startTime} estimatedDuration={estimatedDurationInMillis} noBackground />
                </section>
                {titleComp}
                {topNavLinks}
                {runButton}
                <CloseButton onClick={closeClicked} t={t} />
            </TopNav>
            <HeaderDetails>
                <div className="ResultPageHeader-main u-flex-grow">{props.children}</div>
            </HeaderDetails>
        </BasicHeader>
    );
};

ResultPageHeader.propTypes = {
    className: PropTypes.string,
    children: PropTypes.node,
    status: PropTypes.string,
    onCloseClick: PropTypes.func,
    title: PropTypes.node,
    topNavLinks: PropTypes.node,
    runButton: PropTypes.node,
    startTime: PropTypes.string,
    estimatedDurationInMillis: PropTypes.number,
    t: PropTypes.func,
};
