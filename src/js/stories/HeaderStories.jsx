// @flow

import React from 'react';
import { storiesOf } from '@kadira/storybook';
import WithContext from './WithContext';

import {
    BasicHeader,
    TopNav,
    HeaderDetails,
    PageTabs,
    TabLink } from '../components';

import type { Result } from '../components/status/StatusIndicator';


storiesOf('Header', module)
    .add('Basic', basic)
    .add('StatusColor', statusColors)
;

const Example = (props: {statusColor?: Result}) =>
    <BasicHeader {...props}>
        <TopNav style={{justifyContent: "center"}}>
            This is an example with statusColor {props.statusColor || "(none)"}.
        </TopNav>
        <HeaderDetails style={{justifyContent: "center"}}>
            This is an example with statusColor {props.statusColor || "(none)"}.
        </HeaderDetails>
    </BasicHeader>;

function basic() {

    const tabLinkContext = {
        router: {
            isActive: org => org === '/c',
            createHref: () => "#"
        }
    };

    return (
        <BasicHeader>
            <TopNav>
                <div className="Header-title">
                    <h1>Site navigation, user profile, login / logout</h1>
                </div>
                <nav>
                    <a href="#" className="selected">Anchor</a>
                    <a href="#">Bravo</a>
                    <a href="#">Charlie</a>
                    <a href="#">Xyzzy LTjg</a>
                </nav>
            </TopNav>
            <HeaderDetails>
                <div className="u-flex-grow" style={{ justifyContent: "center" }}>
                    This area is for sub-nav, detailed information, etc.
                </div>
                <PageTabs>
                    <WithContext context={tabLinkContext}><TabLink to="/a">Ainsley</TabLink></WithContext>
                    <WithContext context={tabLinkContext}><TabLink to="/b">Bill</TabLink></WithContext>
                    <WithContext context={tabLinkContext}><TabLink to="/c">Ching He</TabLink></WithContext>
                    <a href="#">Delia</a>
                </PageTabs>
            </HeaderDetails>
        </BasicHeader>
    );
}

function statusColors() {
    return (
        <div>
            <Example/>
            <p>&nbsp;</p>
            <Example statusColor="success"/>
            <p>&nbsp;</p>
            <Example statusColor="failure"/>
            <p>&nbsp;</p>
            <Example statusColor="running"/>
            <p>&nbsp;</p>
            <Example statusColor="queued"/>
            <p>&nbsp;</p>
            <Example statusColor="unstable"/>
            <p>&nbsp;</p>
            <Example statusColor="aborted"/>
            <p>&nbsp;</p>
            <Example statusColor="not_built"/>
            <p>&nbsp;</p>
            <Example statusColor="paused"/>
            <p>&nbsp;</p>
            <Example statusColor="unknown"/>
        </div>
    );
}
