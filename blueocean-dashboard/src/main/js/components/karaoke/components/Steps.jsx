import React, { Component, PropTypes } from 'react';
import { ResultItem, TimeDuration } from '@jenkins-cd/design-language';
import { logging, TimeManager } from '@jenkins-cd/blueocean-core-js';
import { QueuedState, NoSteps } from './QueuedState';
import { calculateFetchAll, calculateLogUrl } from '../../../util/UrlUtils';
import LogConsole from './LogConsole';
import InputStep from './InputStep';

const logger = logging.logger('io.jenkins.blueocean.dashboard.Step');
const timeManager = new TimeManager();

export default class Steps extends Component {
    render() {
        const { t } = this.props;
        const { nodeInformation } = this.props;
        // Early out
        if (!nodeInformation) {
            const queuedMessage = t('rundetail.pipeline.pending.message',
                { defaultValue: 'Waiting for backend to response' });
            return <QueuedState message={queuedMessage} />;
        }
        const { model } = nodeInformation;
        if ( model.length === 0) {
            return (<NoSteps message={t('rundetail.pipeline.nosteps',
                { defaultValue: 'There are no logs' })} />);
        }
        const stepRenderer = model.map((item, index) => <div>{index}</div>);
        return (<div>{ stepRenderer }</div>);
    }
}

export class Nodes extends Component {
    render() {
        const { nodeInformation } = this.props;
        // Early out
        if (!nodeInformation) {
            return null;
        }
        const {
            model,
            nodesBaseUrl,
        } = nodeInformation;
        const { logs, fetchLog, followAlong, url, location, router, t, locale, classicInputUrl } = this.props;
        return (<div>
            {
              model.map((item, index) =>
                <Step
                  {...{
                      key: `${index}${item.id}`,
                      node: item,
                      classicInputUrl,
                      logs,
                      nodesBaseUrl,
                      fetchLog,
                      followAlong,
                      url,
                      location,
                      router,
                      t,
                      locale,
                  }}
                />)
            }
        </div>);
    }
}

Nodes.propTypes = {
    nodeInformation: PropTypes.object.isRequired,
    node: PropTypes.object.isRequired,
    followAlong: PropTypes.bool,
    logs: PropTypes.object,
    location: PropTypes.object,
    fetchLog: PropTypes.func,
    nodesBaseUrl: PropTypes.string,
    router: PropTypes.shape,
    url: PropTypes.string,
    locale: PropTypes.object,
    classicInputUrl: PropTypes.object,
    t: PropTypes.func,
};

export class Node extends Component {
    constructor(props) {
        super(props);
        const node = this.expandAnchor(props);
        this.state = { isFocused: node.isFocused };
    }
    componentWillMount() {
        const { nodesBaseUrl, fetchLog } = this.props;
        const { config = {} } = this.context;
        const node = this.expandAnchor(this.props);
        const {
            durationInMillis,
            state,
            startTime,
        } = node;
        const { durationMillis } = this.durationHarmonize({
            durationInMillis,
            startTime,
            isRunning: state === 'RUNNING' || state === 'PAUSED',
        });
        this.durationMillis = durationMillis;

        if (node && node.isFocused) {
            const fetchAll = node.fetchAll;
            const mergedConfig = { ...config, node, nodesBaseUrl, fetchAll };
            if (!node.isInputStep) {
                fetchLog(mergedConfig);
            }
        }
    }

    componentWillReceiveProps(nextProps) {
        const { logs, nodesBaseUrl, fetchLog, followAlong } = nextProps;
        // Changing state of the node  we want to collapse automatic
        if (nextProps.node.state !== this.props.node.state && nextProps.node.state === 'FINISHED') {
            if (this.state.isFocused) {
                this.setState({ isFocused: false });
            }
        }
        const { config = {} } = this.context;
        const node = this.expandAnchor(nextProps);
        const fetchAll = node.fetchAll;
        const mergedConfig = { ...config, node, nodesBaseUrl, fetchAll };
        if (logs && logs !== this.props.logs || fetchAll) {
            const key = calculateLogUrl(mergedConfig);
            const log = logs ? logs[key] : null;
            if (log && log !== null) {
                // we may have a streaming log
                const number = Number(log.newStart);
                // in case we doing karaoke we want to see more logs
                if ((number > 0 || !log.logArray) && followAlong && !node.isInputStep) {
                    mergedConfig.newStart = log.newStart;
                    // kill current  timeout if any
                    this.clearThisTimeout();
                    this.timeout = setTimeout(() => fetchLog({ ...mergedConfig }), 1000);
                }
            } else if (!log && fetchAll) { // in case the link "full log" is clicked we need to trigger a refetch
                this.clearThisTimeout();
                this.timeout = setTimeout(() => fetchLog({ ...mergedConfig }), 1000);
            }
        }
    }

    componentWillUnmount() {
        this.clearThisTimeout();
    }

    clearThisTimeout() {
        if (this.timeout) {
            clearTimeout(this.timeout);
        }
    }
    durationHarmonize(node) {
        const skewMillis = this.context.config ? this.context.config.getServerBrowserTimeSkewMillis() : 0;
        // the time when we started the run harmonized with offset
        return timeManager.harmonizeTimes({ ...node }, skewMillis);
    }
    /*
     * Calculate whether we need to expand the step due to linking.
     * When we trigger a log-0 that means we want to see the full log
     */
    expandAnchor(props) {
        const { node, location: { hash: anchorName } } = props;
        const isFocused = this.state ? this.state.isFocused : node.isFocused;
        const fetchAll = calculateFetchAll(props);
        const isInputStep = node.input && node.input !== null;
        const general = { ...node, fetchAll, isInputStep };
        // e.g. #step-10-log-1 or #step-10
        if (anchorName) {
            const stepReg = /step-([0-9]{1,})?($|-log-([0-9]{1,})$)/;
            const match = stepReg.exec(anchorName);

            if (match && match[1] && match[1] === node.id) {
                return { ...general, isFocused: true };
            }
        }
        return { ...general, isFocused };
    }

    render() {
        const { logs, nodesBaseUrl, fetchLog, followAlong, url, location, router, t, locale, classicInputUrl } = this.props;
        const node = this.expandAnchor(this.props);
        // Early out
        if (!node || !fetchLog) {
            return null;
        }
        const { config = {} } = this.context;
        const {
            fetchAll,
            title,
            result,
            id,
            state,
            durationInMillis,
            endTime,
            startTime,
            isInputStep = false,
            isFocused = false,
        } = node;
        const resultRun = result === 'UNKNOWN' || !result ? state : result;
        const log = logs ? logs[calculateLogUrl({ ...config, node, nodesBaseUrl, fetchAll })] : null;
        const getLogForNode = () => {
            // in case we do not have logs, or the logs are have no information attached we refetch them
            if (!log || !log.logArray) {
                fetchLog({ ...config, node, nodesBaseUrl });
            }
            this.setState({ isFocused: true });
        };
        const removeFocus = () => {
            this.setState({ isFocused: false });
            // we need to remove the hash on collapse otherwise the result item will not be collapsed
            if (location.hash) {
                delete location.hash;
                router.push(location);
            }
        };
        const scrollToBottom =
                resultRun === 'FAILURE'
                || (resultRun === 'RUNNING' && followAlong)
            ;
        const isRunning = () => resultRun === 'RUNNING' || resultRun === 'PAUSED';
        const { durationMillis } = this.durationHarmonize({
            durationInMillis,
            endTime,
            startTime,
            isRunning: isRunning(),
        });
        logger.debug('time:', {
            responseDuration: durationMillis,
            durationInMillis,
            endTime,
            startTime,
            isRunning: isRunning(),
        });
        const logProps = {
            ...this.props,
            url,
            scrollToBottom,
            key: id,
            prefix: `step-${id}-`,
        };

        const { hasLogs } = node;
        if (log) {
            // in follow along the Full Log button should not be shown, since you see everything already
            if (followAlong) {
                logProps.hasMore = false;
            } else {
                logProps.hasMore = log.hasMore;
            }
            logProps.logArray = log.logArray;
        }

        const logConsoleClass = `logConsole step-${id}`;
        let children = null;
        if (log && !isInputStep) {
            children = <LogConsole {...logProps} />;
        } else if (isInputStep) {
            children = <InputStep {...{ node, classicInputUrl }} />;
        } else if (!log && hasLogs) {
            children = <span>&nbsp;</span>;
        }
        const time = (<TimeDuration
            millis={isRunning() ? this.durationMillis : durationMillis }
            liveUpdate={isRunning()}
            updatePeriod={1000}
            locale={locale}
            displayFormat={t('common.date.duration.display.format', { defaultValue: 'M[ month] d[ days] h[ hours] m[ minutes] s[ seconds]' })}
            liveFormat={t('common.date.duration.format', { defaultValue: 'm[ minutes] s[ seconds]' })}
            hintFormat={t('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' })}
        />);

        return (<div className={logConsoleClass}>
            <ResultItem {...{
                extraInfo: time,
                key: id,
                result: resultRun.toLowerCase(),
                expanded: isFocused,
                label: title,
                onCollapse: removeFocus,
                onExpand: getLogForNode,
            }}
            >
                {children}
            </ResultItem>
        </div>);
    }

}

const { object, func, string, bool, shape } = PropTypes;
Node.propTypes = {
    node: object.isRequired,
    followAlong: bool,
    logs: object,
    location: object,
    fetchLog: func,
    nodesBaseUrl: string,
    router: shape,
    url: string,
    locale: object,
    t: func,
    classicInputUrl: object,
};

Node.contextTypes = {
    config: object.isRequired,
};
