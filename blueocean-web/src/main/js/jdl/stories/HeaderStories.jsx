// @flow

import React from 'react';
import {storiesOf} from '@kadira/storybook';
import WithContext from './WithContext';

import {
    BasicHeader,
    TopNav,
    HeaderDetails,
    PageTabs,
    TabLink
} from '../components';

import type {Result} from '../components/status/StatusIndicator';


storiesOf('Header', module)
    .add('Basic', basic)
    .add('StatusColor', statusColors)
    .add('Split', split)
;

const Width = () => {

    const outer = {
        display: 'block',
        marginTop: '5em',
        marginLeft: 'auto',
        marginRight: 'auto',
        width: '100%',
        maxWidth: '1200px',
        padding: '0 15px',
        background: 'red',
        color: 'white'
    };

    const inner = {
        background: 'rgba(0,0,0,0.3)',
        textAlign: 'center',
        padding: '1em',
    };

    return (
        <div style={outer}>
            <div style={inner}>1200 total</div>
        </div>
    );
};

/**
 * We'll show a single-parent and split header side-by side here, to make sure they look the same. We want to be able
 * to split the normal content page style header into two, because in BO we have a "site header" in blueocean-web, and
 * we need pages located in plugins to be able to control their own "page header" with the HeaderDetails
 */
function split() {

    const containerStyle = {
        display: "flex"
    };

    const colStyle = {
        width: "50%"
    };

    return (
        <div>
            <div style={containerStyle}>
                <div style={colStyle}>
                    <BasicHeader>
                        <TopNav style={{justifyContent: "center"}}>
                            The left and right halves of this...
                        </TopNav>
                        <HeaderDetails style={{justifyContent: "center"}}>
                            ... should appear the same.
                        </HeaderDetails>
                    </BasicHeader>
                </div>
                <div style={colStyle}>
                    <BasicHeader>
                        <TopNav style={{justifyContent: "center"}}>
                            We need to be able to split the basic header in two...
                        </TopNav>
                    </BasicHeader>
                    <BasicHeader>
                        <HeaderDetails style={{justifyContent: "center"}}>
                            ... because it lives in two modules in BlueOcean
                        </HeaderDetails>
                    </BasicHeader>
                </div>
            </div>
            <Width/>
        </div>
    );
}

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
        <div>
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
            <Width/>
        </div>
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
