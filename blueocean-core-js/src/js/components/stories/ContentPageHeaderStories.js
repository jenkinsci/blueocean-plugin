/* eslint-disable */

import React, { Component, PropTypes } from 'react';
import { storiesOf } from '@kadira/storybook';
import { BlueLogo } from '../BlueLogo';
import { ContentPageHeader } from '../ContentPageHeader';
import { SiteHeader } from '../ContentPageHeader';
import { ResultPageHeader } from '../ResultPageHeader';
import { LiveStatusIndicator, WeatherIcon } from '@jenkins-cd/design-language';

storiesOf('Headers', module)
    .add('Logo', logo)
    .add('Dashboard', pageHeaderDashboard)
    .add('Pipeline', pageHeaderPipeline)
    .add('Result', pageHeaderResult)
;

function logo() {
    return (
        <section>
            <h1>100%</h1>
            <div style={{ display: 'flex', background: '#3a70b0', padding: '3em' }}>
                <BlueLogo />
            </div>

            <p>&nbsp;</p>

            <h1>300%</h1>
            <div style={{ display: 'flex', background: '#444', padding: '1em', zoom: '300%' }}>
                <BlueLogo />
            </div>
        </section>
    );
}

function pageHeaderDashboard() {
    const topNavLinks = [
        <a href="#" className="selected">Pipelines</a>,
        <a href="#">Applications</a>,
        <a href="#">Reports</a>,
        <a href="#">Administration</a>,
    ];

    const userComponents = [
        <button className="btn-sign-in">Sign in</button>,
        <button className="btn-sign-up">Sign up</button>,
    ];

    return (
        <div>
            <SiteHeader topNavLinks={topNavLinks} userComponents={userComponents}/>
            <ContentPageHeader>
                <h1>Dashboard</h1>
            </ContentPageHeader>
        </div>
    );
}

function pageHeaderPipeline() {
    const topNavLinks = [
        <a href="#" className="selected">Pipelines</a>,
        <a href="#">Applications</a>,
        <a href="#">Reports</a>,
        <a href="#">Administration</a>,
    ];

    const pageTabLinks = [
        <a href="#" className="selected">Activity</a>,
        <a href="#">Branches</a>,
        <a href="#">Pull Requests</a>,
        <a href="#">Trends</a>,
    ];

    const userComponents = [
        <button className="btn-sign-in">Sign in</button>,
        <button className="btn-sign-up">Sign up</button>,
    ];

    return (
        <div>
            <SiteHeader topNavLinks={topNavLinks} userComponents={userComponents}/>
            <ContentPageHeader pageTabLinks={pageTabLinks}>
                <WeatherIcon score={100} />
                <h1>Lorem / Ipsum / <a href="#">Pipelineum</a></h1>
            </ContentPageHeader>
        </div>
    );
}


function pageHeaderResult() {
    return (
        <section>
            <ExamplePageHeader status="success" />
            <br />
            <ExamplePageHeader status="failure" />
            <br />
            <ExamplePageHeader status="running" />
            <br />
            <ExamplePageHeader status="queued" />
            <br />
            <ExamplePageHeader status="unstable" />
            <br />
            <ExamplePageHeader status="aborted" />
            <br />
            <ExamplePageHeader status="not_built" />
            <br />
            <ExamplePageHeader status="paused" />
            <br />
            <ExamplePageHeader status="unknown" />
            <br />
        </section>
    );

    function ExamplePageHeader(props) {
        const topNavLinks = [
            <a href="#" className="selected">Pipeline</a>,
            <a href="#">Changes</a>,
            <a href="#">Tests</a>,
            <a href="#">Artifacts</a>,
        ];

        const runButton = [
            <button className="btn-secondary">Re-run</button>,
        ];

        const title = <h1>Lorem / Ipsum / {props.status} <a href="#">#211</a></h1>;

        return (
            <ResultPageHeader status={props.status}
              title={title}
              topNavLinks={topNavLinks}
              runButton={runButton}
              onCloseClick={ () => console.log('Ouch! Close me!') }
        >
                [ TODO: build details ]
            </ResultPageHeader>
        );
    }
}
