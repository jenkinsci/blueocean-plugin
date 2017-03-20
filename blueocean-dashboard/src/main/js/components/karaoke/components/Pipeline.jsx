import React, { Component, PropTypes } from 'react';
import { logging, sseConnection } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';
import { QueuedState } from './QueuedState';
import { KaraokeService } from '../index';
import LogToolbar from './LogToolbar';
import Steps from './Steps';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Pipeline');

@observer
export default class Pipeline extends Component {
    constructor(props) {
        super(props);
        this.listener = {};
        this.sseEventHandler = this.sseEventHandler.bind(this);
        this.showPending = true; // Configure flag to show pending or not
    }
    componentWillMount() {
        if (this.props.augmenter) {
            const { augmenter, params: { node } } = this.props;
            this.pager = KaraokeService.pipelinePager(augmenter, { node });
        }
        this.listener.ssePipeline = sseConnection.subscribe('pipeline', this.sseEventHandler);
        this.listener.sseJob = sseConnection.subscribe('job', this.sseEventHandler);
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
        if (this.listener.ssePipeline) {
            sseConnection.unsubscribe(this.listener.ssePipeline);
            delete this.listener.ssePipeline;
        }
        if (this.listener.sseJob) {
            sseConnection.unsubscribe(this.listener.sseJob);
            delete this.listener.sseJob;
        }
    }

    stopKaraoke() {
        logger.debug('stopping karaoke mode, by removing the timeouts on the pager.');
        this.pager.clear();
    }

    /**
     * Listen for pipeline flow node events. We need to re-fetch in case of some events.
     * @param event sse event coming from the backende
     */
    sseEventHandler(event) {
         // we are using try/catch to throw an early out error
        try {
            logger.debug('incoming event', event);
            const jenkinsEvent = event.jenkins_event;
            const { run } = this.props;
            const runId = run.id;
            const bounceNodes = debounce(() => {
                this.pager.fetchNodes({});
            }, 100);
            const bounceSteps = debounce(() => {
                this.pager.fetchCurrentStepUrl();
            }, 100);
             // we get events from the pipeline and the job channel, they have different naming for the id
            //  && event.jenkins_object_id !== runId -> job
            if (event.pipeline_run_id !== runId) {
                logger.warn('early out');
                throw new Error('exit');
            }
            switch (jenkinsEvent) {
            case 'pipeline_step': {
                logger.warn('sse event step fetchCurrentSteps', jenkinsEvent);
                bounceSteps();
                break;
            }
            case 'pipeline_end':
            case 'job_run_ended':
            case 'pipeline_block_end':
            case 'pipeline_stage': {
                logger.warn('sse event block starts refetchNodes', jenkinsEvent);
                bounceNodes({});
                break;
            }
            default: {
                logger.warn('ignoring event', jenkinsEvent);
            }
            }
        } catch (e) {
            // we only ignore the exit error
            if (e.message !== 'exit') {
                logger.error('sse Event has produced an error, will not work as expected.', e);
            }
        }
    }

    render() {
        const { t, run, augmenter, branch, pipeline, router, scrollToBottom, location } = this.props;
        if (run.isQueued()) {
            const queuedMessage = t('rundetail.pipeline.queued.message', { defaultValue: 'Waiting for run to start' });
            return <QueuedState message={queuedMessage} />;
        }
        if (this.pager.pending && this.showPending) {
            logger.debug('abort due to pager pending');
            const queuedMessage = t('rundetail.pipeline.pending.message', { defaultValue: 'Waiting for backend to response' });
            return <QueuedState message={queuedMessage} />;
        }
        // here we decide what to do next if somebody clicks on a flowNode
        const afterClick = (id) => {
            logger.warn('clicked on node with id:', id);
            this.showPending = false; // Configure flag to not show pending anymore
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
        // JENKINS-40526
        const logUrl = this.pager.nodes !== undefined ? `${this.pager.currentNode._links.self.href}log/` : augmenter.generalLogUrl;
        const logFileName = this.pager.nodes !== undefined ? `${this.pager.currentNode.displayName}.log` : augmenter.generalLogUrl;
        logger.debug('displayName', this.pager.currentNode.displayName, logUrl, augmenter.generalLogFileName);
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
                fileName={logFileName}
                url={logUrl}
                title={title}
            />
            { this.pager.steps &&
                <Steps
                    {...{
                        key: this.pager.currentStepsUrl,
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
