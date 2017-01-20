import React, { PropTypes } from 'react';
import {
    BasicHeader,
    TopNav,
    PageTabs,
    HeaderDetails,
} from '@jenkins-cd/design-language';

import { BlueLogo } from './BlueLogo';

// Wrap an array of elements in a parent element without requiring a bunch "key" props
export function _wrap(children, elementOrComponent = 'div', props = {}) {
    if (!children) {
        return null;
    }
    const childArray = Array.isArray(children) ? children : [children];
    return React.createElement(elementOrComponent, props, ...childArray);
}

export const ContentPageHeader = props => {
    const topNavLinks = _wrap(props.topNavLinks, 'nav');
    const userComponents = _wrap(props.userComponents, 'div', { className: 'ContentPageHeader-user' });
    const pageTabLinks = _wrap(props.pageTabLinks, PageTabs);

    return (
        <BasicHeader classname="ContentPageHeader">
            <TopNav>
                <BlueLogo />
                <div className="u-flex-grow" />
                { topNavLinks }
                { userComponents }
            </TopNav>
            <HeaderDetails>
                <div className="ContentPageHeader-main u-flex-grow">
                    { props.children }
                </div>
                { pageTabLinks }
            </HeaderDetails>
        </BasicHeader>
    );
};

ContentPageHeader.propTypes = {
    topNavLinks: PropTypes.node,
    userComponents: PropTypes.node,
    pageTabLinks: PropTypes.node,
    children: PropTypes.node,
};

export default ContentPageHeader;
