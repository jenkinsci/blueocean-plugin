import React, { Component, PropTypes } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { EmptyStateView } from '@jenkins-cd/design-language';
import Extensions from '@jenkins-cd/js-extensions';
import { observer } from 'mobx-react';
import { KaraokeService } from '../index';
import LogToolbar from './LogToolbar';
import Steps from './Steps';
const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Pipeline');

// FIXME: needs to use i18n for translations
const QueuedState = () => (
    <EmptyStateView tightSpacing>
        <p>
            <Icon {...{
                size: 20,
                icon: 'timer',
                style: { fill: '#fff' },
            }}
            />
            <span className="waiting">Waiting for run to start.</span>
        </p>
    </EmptyStateView>
);

@observer
export default class Pipeline extends Component {
    componentWillMount() {
        if (this.props.augmenter) {
            this.fetchData(this.props);
        }
    }

    componentWillReceiveProps(nextProps) {
        if (!nextProps.followAlong && this.props.followAlong) {
            this.stopKaraoke();
        }
        if (nextProps.run.isCompleted() && !nextProps.augmenter.run.isCompleted()) {
            logger.debug('re-fetching since result changed and we want to display the full log');
            this.pager.fetchGeneralLog({});
        }
    }

    componentWillUnmount() {
        this.stopKaraoke();
    }

    stopKaraoke() {
        logger.debug('stopping karaoke mode, by removing the timeouts on the pager.');
        this.pager.clear();
    }

    fetchData(props) {
        const { augmenter, followAlong } = props;
        this.pager = KaraokeService.pipelinePager(augmenter, followAlong);
    }

    render() {
        const { t, run, augmenter, branch, pipeline, followAlong, router, scrollToBottom,  } = this.props;
        if (run.isQueued()) {
            return <QueuedState />;
        }
        if (this.pager.pending) {
            logger.debug('abort due to pager pending');
            return null;
        }
        logger.warn('props', this.pager.nodes === undefined);
        // here we decide what to do next if somebody clicks on a flowNode
        const afterClick = (id) => {
            logger.debug('clicked on node with id:', id);
        };
        //
        return (<div>
            { this.pager.nodes !== undefined &&
                <Extensions.Renderer
                    extensionPoint="jenkins.pipeline.run.result"
                    selectedStage={this.pager.currentNode}
                    callback={afterClick}
                    nodes={this.pager.nodes.data.model}
                    pipelineName={pipeline.displayName}
                    branchName={augmenter.isMultiBranchPipeline ? branch : undefined}
                    runId={run.id}
                    run={run}
                    t={t}
                />
            }
            <LogToolbar
                fileName={augmenter.generalLogFileName}
                url={augmenter.generalLogUrl}
                title={t('rundetail.pipeline.logsF', { defaultValue: 'Logs FIXME' })}
            />
            { this.pager.steps &&
                <Steps
                    nodeInformation={this.pager.steps.data}
                    {...{
                        followAlong,
                        router,
                        scrollToBottom,
                        ...this.props,
                        ...this.state,
                        url: augmenter.generalLogUrl,
                    }}
                />
            }
        </div>);
    }
}

Pipeline.propTypes = {
    augmenter: PropTypes.object,
    pipeline: PropTypes.object,
    branch: PropTypes.string,
    run: PropTypes.object,
    t: PropTypes.func,
    router: PropTypes.shape,
    location: PropTypes.shape,
    followAlong: PropTypes.bol,
    scrollToBottom: PropTypes.bol,
};
