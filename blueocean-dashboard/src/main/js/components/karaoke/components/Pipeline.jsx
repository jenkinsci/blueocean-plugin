import React, { Component, PropTypes } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { observer } from 'mobx-react';
import { QueuedState } from './QueuedState';
import { KaraokeService } from '../index';
import LogToolbar from './LogToolbar';
import Steps from './Steps';
const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Pipeline');

@observer
export default class Pipeline extends Component {
    componentWillMount() {
        if (this.props.augmenter) {
            const { augmenter, params: { node } } = this.props;
            this.pager = KaraokeService.pipelinePager(augmenter, { node });
        }
    }

    componentWillReceiveProps(nextProps) {
        if (!nextProps.augmenter.karaoke) {
            logger.debug('stopping karaoke mode.');
            this.stopKaraoke();
        }
        if ((nextProps.run.isCompleted() && !nextProps.augmenter.run.isCompleted()) || (nextProps.run !== this.props.run)) {
            logger.debug('re-fetching since result changed and we want to display the full log and correct result states');
            if (nextProps.run !== this.props.run) {
                logger.debug('Need to set new Run. Happens when e.g. re-run.');
                nextProps.augmenter.setRun(nextProps.run);
            }
            this.pager.fetchNodes({});
        }
        if (nextProps.params.node !== this.props.params.node) {
            logger.debug('Need to fetch new nodes.');
            this.pager.fetchNodes({ node: nextProps.params.node });
        }
    }

    componentWillUnmount() {
        this.stopKaraoke();
    }

    stopKaraoke() {
        logger.debug('stopping karaoke mode, by removing the timeouts on the pager.');
        this.pager.clear();
    }

    render() {
        const { t, run, augmenter, branch, pipeline, router, scrollToBottom, location } = this.props;
        if (run.isQueued()) {
            const queuedMessage = t('rundetail.pipeline.queued.message', { defaultValue: 'Waiting for run to start' });
            return <QueuedState message={queuedMessage} />;
        }
        if (this.pager.pending) {
            logger.debug('abort due to pager pending');
            const queuedMessage = t('rundetail.pipeline.pending.message', { defaultValue: 'Waiting for backend to response' });
            return <QueuedState message={queuedMessage} />;
        }
        logger.warn('props', this.pager.nodes === undefined);
        // here we decide what to do next if somebody clicks on a flowNode
        const afterClick = (id) => {
            logger.debug('clicked on node with id:', id);
            const nextNode = this.pager.nodes.data.model.filter((item) => item.id === id)[0];
            // remove trailing /
            const pathname = location.pathname.replace(/\/$/, '');
            let nextPath;

            if (pathname.endsWith('pipeline')) {
                nextPath = `${pathname}/${id}`;
            } else {
                // remove last bits
                const pathArray = pathname.split('/');
                pathArray.pop();
                pathArray.shift();
                nextPath = `/${pathArray.join('/')}/${id}`;
            }
            // check whether we have a parallel node - DO WE WHY
            // const isParallel = false;//this.isParallel(nodeInfo);
            location.pathname = nextPath;
            logger.debug('redirecting now to:', location.pathname);
            // see whether we need to update the state
            if ((nextNode.state === 'FINISHED') && this.props.augmenter.karaoke) {
                this.props.augmenter.setKaraoke(false);
            }
            if (nextNode.state !== 'FINISHED' && !this.props.augmenter.karaoke) {
                this.props.augmenter.setKaraoke(true);
            }
            router.push(location);
        };
        const title = this.pager.nodes !== undefined ? t('rundetail.pipeline.steps', {
            defaultValue: 'Steps ',
            0: this.pager.currentNode.displayName,
        }) : '';
        logger.debug('displayName', this.pager.currentNode.displayName);
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
                title={title}
            />
            { this.pager.steps &&
                <Steps
                    {...{
                        nodeInformation: this.pager.steps.data,
                        followAlong: augmenter.karaoke,
                        augmenter,
                        t,
                        scrollToBottom,
                        router,
                        location,
                    }}
                />
            }
        </div>);
    }
}
// nodeInformation: this.pager.steps.data
Pipeline.propTypes = {
    augmenter: PropTypes.object,
    pipeline: PropTypes.object,
    branch: PropTypes.string,
    run: PropTypes.object,
    t: PropTypes.func,
    router: PropTypes.shape,
    location: PropTypes.shape,
    scrollToBottom: PropTypes.bol,
    params: PropTypes.object,
};
