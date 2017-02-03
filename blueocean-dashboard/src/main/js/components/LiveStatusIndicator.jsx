import React, { Component } from 'react';
import { LiveStatusIndicator as LiveStatusIndicatorJdl } from '@jenkins-cd/design-language';
import { i18nTranslator, logging } from '@jenkins-cd/blueocean-core-js';
import { TimeManager } from '../util/serverBrowserTimeHarmonize';
/**
 * Translate function
 */
const t = i18nTranslator('blueocean-dashboard');
const timeManager = new TimeManager();
const logger = logging.logger('io.jenkins.blueocean.dashboard.LiveStatusIndicator');

export class LiveStatusIndicator extends  Component {
    render() {
        const title = "FIXME: duration";
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
