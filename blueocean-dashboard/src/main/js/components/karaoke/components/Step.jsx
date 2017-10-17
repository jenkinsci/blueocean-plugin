import React, { Component, PropTypes } from 'react';
import { ResultItem, TimeDuration } from '@jenkins-cd/design-language';
import { AppConfig, TimeManager } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import LogConsole from './LogConsole';
import InputStep from './InputStep';
import { prefixIfNeeded } from '../urls/prefixIfNeeded';
import { LogStore } from '../services/LogStore';
import { Logger } from '../../../util/Logger';
const log = new Logger('pipeline.run.render.step');

const timeManager = new TimeManager();

function createStepLabel(step) {
    const { displayName, displayDescription } = step;

    if (displayDescription) {
        return [
            <span className="result-item-label-desc" title={displayDescription}>{displayDescription}</span>,
            <span className="result-item-label-name">&mdash; {displayName}</span>,
        ];
    }

    return displayName;
}

@observer
export class Step extends Component {
    constructor(props) {
        super(props);
        this.state = {
            expanded: this.isFocused(props),
        };
    }

    /**
     * Mainly implemented because
     * - we need to create a reference time for running jobs
     */
    componentWillMount() {
        const { step } = this.props;
        // needed for running steps as reference
        this.durationInMillis = (this.durationHarmonize(step)).durationInMillis;
        log.debug('durationInMillis mounting', this.durationInMillis);
        this.componentWillReceiveProps(this.props);
    }

    /**
     * Mainly implemented due to fetch full log `start=0` for a step
     * @param nextProps
     */
    componentWillReceiveProps(nextProps) {
        const expanded = this.isFocused(nextProps);
        const nextStart = nextProps.location && nextProps.location.query ? nextProps.location.query.start : undefined;
        const currentStart = this.props.location && this.props.location.query ? this.props.location.query.start : undefined;
        log.debug('newProps mate', nextStart, currentStart);
        if (!this.logStore || (currentStart !== nextStart && nextStart !== undefined)) {
            log.debug('fetching log with props', nextProps);
            this.componentWillUnmount();
            this.logStore = new LogStore(nextProps.step.logUrl, nextStart);
            if (expanded) {
                this.props.liveUpdate.addUpdater(this._updater = () => this.logStore.fetch());
            } else {
                this.logStore.fetch();
            }
        }
        if (this.state.expanded !== expanded) {
            this.setState({ expanded });
        }
    }

    componentWillUnmount() {
        if (this._updater) {
            log.debug('stopping log tail');
            this.props.liveUpdate.removeUpdater(this._updater);
        }
    }

    expandLog() {
        this.logStore = new LogStore(this.props.step.logUrl);
        this.logStore.fetch();
        // we are now want to expand the result item
        this.setState({ expanded: true });
    }

    // needed to calculated running times
    durationHarmonize(step) {
        const skewMillis = AppConfig.getServerBrowserTimeSkewMillis();
        // the time when we started the run harmonized with offset
        return timeManager.harmonizeTimes({ ...step }, skewMillis);
    }

    /*
     * Calculate whether we need to expand the step due to linking.
     * When we trigger a log-0 that means we want to see the full log
     */
    isFocused(props) {
        const { step, location: { hash: anchorName }, isFocused: propFocus } = props;
        const stepFocus = step.isFocused !== undefined && step.isFocused;
        const stateFocus = this.state ? this.state.expanded : propFocus;
        let isFocused = stateFocus !== undefined ? stateFocus : stepFocus;
        // e.g. #step-10-log-1 or #step-10
        if (anchorName) {
            log.debug('expandAnchor', anchorName);
            const stepReg = /step-([0-9]{1,})?($|-log-([0-9]{1,})$)/;
            const match = stepReg.exec(anchorName);

            if (match && match[1] && match[1] === step.id) {
                isFocused = true;
            }
        }
        return isFocused || false;
    }

    collapseLog() {
        this.setState({ expanded: false });
        // we need to remove the hash on collapse otherwise the result item will not be collapsed
        if (location.hash) {
            delete location.hash;
            this.context.router.push(location);
        }
    }

    render() {
        const { step, locale, router, location, t, scrollToBottom } = this.props;

        if (step === undefined || !step) {
            return null;
        }
        const { durationInMillis } = this.durationHarmonize(step);
        const isFocused = this.isFocused(this.props);
        const { data: logArray, hasMore } = this.logStore.log || {};
        let children = null;

        // console.log('isFocused', isFocused, 'expanded', this.state.expanded, 'scrollTobottom', scrollToBottom, 'following:', pipelineView.following);

        if (logArray && !step.isInputStep) {
            const currentLogUrl = prefixIfNeeded(step.logUrl);
            log.debug('Updating children');
            children = (<LogConsole {...{
                t,
                router,
                location,
                hasMore,
                scrollToBottom,
                logArray,
                currentLogUrl,
                key: step.logUrl,
                prefix: `step-${step.id}-`,
            }}
            />);
        } else if (step.isInputStep) {
            children = <InputStep {...{ step, key: 'step' }} />;
        } else if (!logArray && step.hasLogs) {
            children = <span key={'span'}>&nbsp;</span>;
        }
        // some ATH hook enhancements
        const logConsoleClass = `logConsole step-${step.id}`;
        // duration calaculations
        const duration = step.isRunning ? this.durationInMillis : durationInMillis;
        log.debug('duration', duration, step.isRunning);
        const time = (<TimeDuration
            millis={duration }
            liveUpdate={step.isRunning}
            updatePeriod={1000}
            locale={locale}
            displayFormat={t('common.date.duration.display.format', { defaultValue: 'M[ month] d[ days] h[ hours] m[ minutes] s[ seconds]' })}
            liveFormat={t('common.date.duration.format', { defaultValue: 'm[ minutes] s[ seconds]' })}
            hintFormat={t('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' })}
        />);

        return (<div className={logConsoleClass}>
            <ResultItem {...{
                extraInfo: time,
                key: step.key,
                result: step.computedResult.toLowerCase(),
                expanded: isFocused,
                label: createStepLabel(step),
                onCollapse: () => this.collapseLog(),
                onExpand: () => this.expandLog(),
            }}
            >
                { children }
            </ResultItem>
        </div>);
    }
}

Step.propTypes = {
    pipelineView: PropTypes.object,
    liveUpdate: PropTypes.object,
    step: PropTypes.object.isRequired,
    location: PropTypes.object,
    router: PropTypes.shape,
    locale: PropTypes.object,
    t: PropTypes.func,
    scrollToBottom: PropTypes.bool,
    isFocused: PropTypes.object,
};
