import React, { Component, PropTypes } from 'react';
import { LiveStatusIndicator as LiveStatusIndicatorJdl } from '@jenkins-cd/design-language';
import { TimeHarmonizer as timeHarmonizer } from '../components/TimeHarmonizer';
import logging from '../logging';
const logger = logging.logger('io.jenkins.blueocean.core.LiveStatusIndicator');

export class LiveStatusIndicator extends Component {
    render() {
        const { result, getI18nTitle } = this.props;
        const title = getI18nTitle(result);
        logger.debug('i18n title', title);
        return (
            <div title={title}>
                <LiveStatusIndicatorJdl {...this.props} />
            </div>
        );
    }
}

LiveStatusIndicator.propTypes = {
    result: PropTypes.string,
    percentage: PropTypes.number,
    width: PropTypes.string,
    height: PropTypes.string,
    noBackground: PropTypes.bool,
    startTime: PropTypes.string,
    estimatedDuration: PropTypes.number,
    durationInMillis: PropTypes.number,
    getI18nTitle: PropTypes.func,
};

export default timeHarmonizer(LiveStatusIndicator);
