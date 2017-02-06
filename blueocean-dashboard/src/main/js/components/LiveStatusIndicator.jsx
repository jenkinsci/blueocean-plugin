import React, { Component, PropTypes } from 'react';
import { LiveStatusIndicator as LiveStatusIndicatorJdl } from '@jenkins-cd/design-language';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { timeHarmonizer } from './TimeHarmonizer';
const logger = logging.logger('io.jenkins.blueocean.dashboard.LiveStatusIndicator');

export class LiveStatusIndicator extends Component {

    render() {
        const { result } = this.props;
        const title = this.getI18nTitle(result);
        logger.debug('i18n title', title);

        return (
            <div title={title}>
                <LiveStatusIndicatorJdl { ... this.props } />
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
    duration: PropTypes.number,
};

LiveStatusIndicator.contextTypes = {
    config: PropTypes.object.isRequired,
};

export default timeHarmonizer(LiveStatusIndicator);
