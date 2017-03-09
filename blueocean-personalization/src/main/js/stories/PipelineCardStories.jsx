/**
 * Created by cmeyers on 6/28/16.
 */
import React, { PropTypes } from 'react';
import { action, storiesOf } from '@kadira/storybook';
import moment from 'moment';
import { DEBUG } from '@jenkins-cd/blueocean-core-js';
DEBUG.enableMocksForI18n();

import { PipelineCard } from '../components/PipelineCard';

const style = {
    padding: '15px',
    maxWidth: '1200px'
};

const style2 = {
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


storiesOf('PipelineCard', module)
    .addDecorator(story => <Context>{story()}</Context>)
    .add('all states', () => {
        // const statuses = 'SUCCESS,QUEUED,RUNNING,FAILURE,ABORTED,UNSTABLE,NOT_BUILT,UNKNOWN'.split(',');

        const startTime = moment().subtract(60, 'seconds').toISOString();
        const estimatedDuration = 1000 * 60 * 5; // 5 mins

        const running = JSON.parse(JSON.stringify(pipeline));
        running.latestRun.estimatedDuration = estimatedDuration;
        running.latestRun.startTime = startTime;
        running.latestRun.state = 'RUNNING';

        return (
            <div style={style}>
            { statuses.map(state => {
                const clone = JSON.parse(JSON.stringify(pipeline));
                clone.latestRun.state = state;

                return (
                    <div key={state} style={style2}>
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
