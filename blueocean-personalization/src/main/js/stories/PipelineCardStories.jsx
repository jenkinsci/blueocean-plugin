/**
 * Created by cmeyers on 6/28/16.
 */
import React, { PropTypes } from 'react';
import { action, storiesOf } from '@kadira/storybook';
import moment from 'moment';
import { DEBUG } from '@jenkins-cd/blueocean-core-js';
DEBUG.enableMocksForI18n();

import { PipelineCard, PipelineCardRenderer } from '../components/PipelineCard';

const outerStyle = {
    padding: '15px',
    maxWidth: '1200px'
};

const cardWrapStyle = {
    paddingBottom: '10px'
};

const pipeline = {
    _links: {
        self: {
            href: '/a/b/c',
        },
    },
    _capabilities: ['io.jenkins.blueocean.rest.model.BlueBranch'],
    organization: 'Jenkins',
    name: 'master',
    fullName: 'blueocean/master',
    fullDisplayName: 'blueocean/master',
    branch: 'feature/JENKINS-123',
    commitId: '447d8e1',
    favorite: true,
    latestRun: {
        result: 'UNKNOWN',
        state: 'UNKNOWN',
        commitId: '447d8e1',
    },
};

class Context extends React.Component {
    getChildContext() {
        return {
            config: {
                getServerBrowserTimeSkewMillis: () => 0,
            },
        };
    }

    render() {
        return this.props.children;
    }
}

Context.propTypes = {
    children: PropTypes.node,
};

Context.childContextTypes = {
    config: PropTypes.object,
};

const statuses = [
    'SUCCESS',
    // 'QUEUED',
    // 'RUNNING',
    'FAILURE',
    'ABORTED',
    'UNSTABLE',
    'NOT_BUILT',
    'UNKNOWN'
];

const startTime = moment().subtract(60, 'seconds').toISOString();
const estimatedDuration = 1000 * 60 * 5; // 5 mins

storiesOf('PipelineCard', module)
    .addDecorator(story => <Context>{story()}</Context>)
    .add('Placeholder', () => <div>TODO: Remove me</div>)
    .add('PipelineCardRenderer', rendererExamples)
    .add('PipelineCard', () => {
        const running = JSON.parse(JSON.stringify(pipeline));
        running.latestRun.estimatedDuration = estimatedDuration;
        running.latestRun.startTime = startTime;
        running.latestRun.state = 'RUNNING';

        return (
            <div style={outerStyle}>
            { statuses.map(state => {
                const clone = JSON.parse(JSON.stringify(pipeline));
                clone.latestRun.state = state;

                return (
                    <div key={state} style={cardWrapStyle}>
                        <PipelineCard
                          runnable={clone}
                          onRunClick={action('run')}
                          onFavoriteToggle={action('toggle')}
                        />
                    </div>
                );
            }) }
                {/*<PipelineCard*/}
                  {/*runnable={running}*/}
                  {/*onRunClick={action('run')}*/}
                  {/*onFavoriteToggle={action('toggle')}*/}
                {/*/>*/}
            </div>
        );
    });

function rendererExamples() {

    document.body.style.backgroundColor = '#ccc'; // To see the white contents when things go wrong :D

    const displayPath = 'Build name/with/many/and-some-super-super-really-flaming-long-like-you-wouldnt-believe-it/nested/slashes';
    const branchText = 'Branch name';
    const commitText = 'C0MM17H45H';
    const timeText = 'Some time ago';
    const noise = ' Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce ullamcorper rutrum ipsum nec ' +
        'mollis. Suspendisse imperdiet nisi eget convallis condimentum.';

    return (
        <div style={outerStyle}>
            { showRenderer('SUCCESS', displayPath, branchText, commitText, timeText) }
            { showRenderer('PAUSED', displayPath + noise, branchText + noise, commitText + noise, timeText + noise) }

        </div>
    );
}

function showRenderer(status, displayPath, branchText, commitText, timeText) {

    const clone = JSON.parse(JSON.stringify(pipeline));
    clone.latestRun.state = status;

    // const status = clone.latestRun.state;
    // const displayPath = 'Build name';
    // const branchText = 'Branch name';
    // const commitText = 'C0MM17H45H';
    // const timeText = 'Some time ago';
    // const displayPath = 'Build name asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd end';
    // const branchText = 'Branch name asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd end';
    // const commitText = 'C0MM17H45H asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd end';
    // const timeText = 'Some time ago asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd asdasd end';
    const favoriteChecked = !!Math.round(Math.random());
    const runnableItem = clone;
    const latestRun = JSON.parse(JSON.stringify(clone.latestRun));

    return (
        <div style={cardWrapStyle}>
            <PipelineCardRenderer status={status}
                                  startTime={startTime}
                                  estimatedDuration={estimatedDuration}
                                  activityUrl="/job/activities"
                                  displayPath={displayPath}
                                  branchText={branchText}
                                  commitText={commitText}
                                  timeText={timeText}
                                  favoriteChecked={favoriteChecked}
                                  runnableItem={runnableItem}
                                  latestRun={latestRun}/>
        </div>
    );
}
