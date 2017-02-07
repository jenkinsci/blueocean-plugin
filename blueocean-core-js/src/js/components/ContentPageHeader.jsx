import React, { PropTypes } from 'react';
import {
    BasicHeader,
    TopNav,
    PageTabs,
    HeaderDetails,
} from '@jenkins-cd/design-language';

import { BlueLogo } from './BlueLogo';

// Wrap an array of elements in a parent element without requiring a bunch "key" props
// FIXME: This should be strengthened a little, and promoted to JDL with some tests
export function _wrap(children, elementOrComponent = 'div', props = {}) {
    if (!children) {
        return null;
    }

    const childArray = Array.isArray(children) ? children : [children];
    return React.createElement(elementOrComponent, props, ...childArray);
}

export const SiteHeader = props => {
    const topNavLinks = _wrap(props.topNavLinks, 'nav');
    const userComponents = _wrap(props.userComponents, 'div', { className: 'ContentPageHeader-user' });

    return (
        <BasicHeader className="ContentPageHeader">
            <TopNav>
                <BlueLogo />
                <div className="u-flex-grow" />
                { topNavLinks }
                { userComponents }
            </TopNav>
        </BasicHeader>
    );
};

SiteHeader.propTypes = {
    topNavLinks: PropTypes.node,
    userComponents: PropTypes.node,
    children: PropTypes.node,
};

export const ContentPageHeader = props => {
    const pageTabLinks = _wrap(
        props.pageTabLinks,
        PageTabs,
        { base: props.pageTabBase }
    );

    return (
        <BasicHeader className="ContentPageHeader">
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
    pageTabLinks: PropTypes.node,
    children: PropTypes.node,
    pageTabBase: PropTypes.string,
};

export default ContentPageHeader;
