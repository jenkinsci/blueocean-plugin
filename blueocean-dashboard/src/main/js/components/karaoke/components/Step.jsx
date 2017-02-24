import React, { Component, PropTypes } from 'react';
import { ResultItem, TimeDuration } from '@jenkins-cd/design-language';
import { logging, TimeManager } from '@jenkins-cd/blueocean-core-js';
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
        augmenter.step = step;
        this.pager = KaraokeService.logPager(augmenter);
    }

    componentWillMount() {
        // needed for running steps as reference
        this.durationMillis = this.durationHarmonize(this.props.step);
    }

    durationHarmonize(step) {
        const skewMillis = this.context.config ? this.context.config.getServerBrowserTimeSkewMillis() : 0;
        // the time when we started the run harmonized with offset
        return timeManager.harmonizeTimes({ ...step }, skewMillis);
    }

    render() {
        const { step, augmenter, locale, router, location, t, scrollToBottom } = this.props;
        if (step === undefined || !step) {
            return null;
        }
        const logConsoleClass = `logConsole step-${step.id}`;
        const { durationMillis } = this.durationHarmonize(step);
        const isInputStep = step.input && step.input !== null;
        const { data: log, hasMore } = this.pager.log || {};
        logger.warn('arghhhhh');
        let children = null;
        if (log && !isInputStep) {
            debugger;
            children = (<LogConsole {...{
                t,
                router,
                location,
                hasMore,
                scrollToBottom,
                log,
                key: step.logUrl,
            }}
            />);
        } else if (isInputStep) {
            children = <InputStep {...{ step }} />;
        } else if (!log && step.hasLogs) {
            children = <span>&nbsp;</span>;
        }
        const getLogForNode = () => {
            logger.debug('FIXME implement getLogForNode');
            if (!this.pager.log) {
                this.pager.fetchLog(augmenter.karaoke);
            }
        };
        const removeFocus = () => {
            // we need to remove the hash on collapse otherwise the result item will not be collapsed
            if (location.hash) {
                delete location.hash;
                router.push(location);
            }
        };
        const time = (<TimeDuration
            millis={augmenter.run.isRunning() ? this.durationMillis : durationMillis }
            liveUpdate={augmenter.run.isRunning()}
            updatePeriod={1000}
            locale={locale}
            displayFormat={t('common.date.duration.display.format', { defaultValue: 'M[ month] d[ days] h[ hours] m[ minutes] s[ seconds]' })}
            liveFormat={t('common.date.duration.format', { defaultValue: 'm[ minutes] s[ seconds]' })}
            hintFormat={t('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' })}
        />);
        return (<div className={logConsoleClass} key={step.id} >
            <ResultItem {...{
                extraInfo: time,
                key: step.id,
                result: augmenter.run.getComputedResult().toLowerCase(),
                expanded: step.isFocused || false,
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

Step.contextTypes = {
    config: PropTypes.object.isRequired,
};

Step.propTypes = {
    augmenter: PropTypes.object,
    step: PropTypes.object.isRequired,
    location: PropTypes.object,
    router: PropTypes.shape,
    locale: PropTypes.object,
    t: PropTypes.func,
    scrollToBottom: PropTypes.bool,
};
