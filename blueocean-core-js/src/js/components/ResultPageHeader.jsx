import React, { PropTypes } from 'react';
import {
    BasicHeader,
    TopNav,
    HeaderDetails,
    LiveStatusIndicator,
} from '@jenkins-cd/design-language';

import { _wrap } from './ContentPageHeader';

// Exported from Zeplin, path could use a little cleanup
const CloseButton = props => (
    <svg className="ResultPageHeader-close" width="24px" height="24px"
      viewBox="0 0 24 24"
      onClick={props.onClick}
      version="1.1"
    >
        <g strokeWidth="1">
            <polygon points="19 6.415 17.585 5 12 10.585 6.415 5 5 6.415 10.585 12 5 17.585 6.415 19 12 13.415 17.585 19 19 17.585 13.415 12" />
        </g>
    </svg>
);

CloseButton.propTypes = {
    onClick: PropTypes.func,
};

export const ResultPageHeader = props => {
    const {
        status = 'unknown',
        run,
        title,
        onCloseClick,
    } = props;

    const closeClicked = () => {
        if (onCloseClick) {
            onCloseClick();
        }
    };

    const startTime = run && run.startTime || null;
    const estimatedDurationInMillis = run && run.estimatedDurationInMillis || null;

    const titleComp = _wrap(title, 'div', { className: 'ResultPageHeader-title u-flex-grow' });
    const topNavLinks = _wrap(props.topNavLinks, 'nav');
    const runButton = _wrap(props.runButton, 'div', { className: 'ResultPageHeader-run' });

    return (
        <BasicHeader className="ResultPageHeader" statusColor={status}>
            <TopNav>
                <section className="ResultPageHeader-indicator status inverse">
                    <LiveStatusIndicator result={status} startTime={startTime}
                      estimatedDuration={estimatedDurationInMillis}
                      noBackground
                    />
                </section>
                { titleComp }
                { topNavLinks }
                { runButton }
                <CloseButton onClick={closeClicked} />
            </TopNav>
            <HeaderDetails>
                <div className="ResultPageHeader-main u-flex-grow">
                    { props.children }
                </div>
            </HeaderDetails>
        </BasicHeader>
    );
};

ResultPageHeader.propTypes = {
    children: PropTypes.node,
    status: PropTypes.string,
    run: PropTypes.object,
    onCloseClick: PropTypes.func,
    title: PropTypes.node,
    topNavLinks: PropTypes.node,
    runButton: PropTypes.node,
};
