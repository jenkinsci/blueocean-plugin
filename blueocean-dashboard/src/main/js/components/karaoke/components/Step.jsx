import React, { Component, PropTypes } from 'react';
import { ResultItem, TimeDuration } from '@jenkins-cd/design-language';
import { AppConfig, logging, TimeManager } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import { KaraokeService } from '../index';
import LogConsole from './LogConsole';
import InputStep from './InputStep';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Step');
const timeManager = new TimeManager();

@observer
export class Step extends Component {
    constructor(props) {
        super(props);
        const { augmenter, step } = props;
        this.pager = KaraokeService.logPager(augmenter, step);
        logger.log('isFocused', step.isFocused);
        this.state = {
            expanded: step.isFocused !== undefined && step.isFocused,
        };
    }

    componentWillMount() {
        // needed for running steps as reference
        this.durationMillis = (this.durationHarmonize(this.props.step)).durationMillis;
        logger.debug('durationMillis mounting', this.durationMillis);
    }

    durationHarmonize(step) {
        const skewMillis = AppConfig.getServerBrowserTimeSkewMillis();
        // the time when we started the run harmonized with offset
        return timeManager.harmonizeTimes({ ...step }, skewMillis);
    }

    render() {
        const { step, augmenter, locale, router, location, t, scrollToBottom } = this.props;
        if (step === undefined || !step) {
            return null;
        }
        const { durationMillis } = this.durationHarmonize(step);
        const { data: logArray, hasMore } = this.pager.log || {};
        let children = null;
        if (logArray && !step.isInputStep) {
            logger.warn('Updating children');
            children = (<LogConsole {...{
                t,
                router,
                location,
                hasMore,
                scrollToBottom,
                logArray,
                key: step.logUrl,
            }}
            />);
        } else if (step.isInputStep) {
            children = <InputStep {...{ step, key: 'step' }} />;
        } else if (!logArray && step.hasLogs) {
            children = <span key={'span'}>&nbsp;</span>;
        }
        const getLogForNode = () => {
            logger.warn('FIXME implement getLogForNode');
            if (!this.pager.logArray) {
                this.pager.fetchLog({ followAlong: augmenter.karaoke, url: step.logUrl });
                this.setState({ expanded: true });
            }
        };
        const removeFocus = () => {
            // we need to remove the hash on collapse otherwise the result item will not be collapsed
            if (location.hash) {
                delete location.hash;
                router.push(location);
            }
        };
        const logConsoleClass = `logConsole step-${step.id}`;
        const duration = step.isRunning ? this.durationMillis : durationMillis;
        logger.warn('duration', duration, step.isRunning);
        const time = (<TimeDuration
            millis={duration }
            liveUpdate={step.isRunning}
            updatePeriod={1000}
            locale={locale}
            displayFormat={t('common.date.duration.display.format', { defaultValue: 'M[ month] d[ days] h[ hours] m[ minutes] s[ seconds]' })}
            liveFormat={t('common.date.duration.format', { defaultValue: 'm[ minutes] s[ seconds]' })}
            hintFormat={t('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' })}
        />);
        if (this.pager.pending) {
            logger.debug('nulllllllllllll');
            return null;
        }
        return (<div className={logConsoleClass} key={this.pager.currentLogUrl}>
            <ResultItem {...{
                extraInfo: time,
                key: step.key,
                result: step.computedResult.toLowerCase(),
                expanded: this.state.expanded,
                label: step.title,
                onCollapse: removeFocus,
                onExpand: getLogForNode,
            }}
            >
                { children }
            </ResultItem>
        </div>);
    }
}

Step.propTypes = {
    augmenter: PropTypes.object,
    step: PropTypes.object.isRequired,
    location: PropTypes.object,
    router: PropTypes.shape,
    locale: PropTypes.object,
    t: PropTypes.func,
    scrollToBottom: PropTypes.bool,
};
