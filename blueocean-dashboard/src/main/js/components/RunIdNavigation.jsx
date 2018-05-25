import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/design-language';
import { UrlBuilder } from '@jenkins-cd/blueocean-core-js';
import { Link } from 'react-router';
import RunIdCell from './RunIdCell';

export class RunIdNavigation extends Component {
    render() {
        const { run, pipeline, branchName, t } = this.props;

        const nextRunId = run._links.nextRun ? /[\/].*runs\/*([0-9]*)/g.exec(run._links.nextRun.href)[1] : '';
        const prevRunId = run._links.prevRun ? /[\/].*runs\/*([0-9]*)/g.exec(run._links.prevRun.href)[1] : '';

        const nextRunUrl = nextRunId ? UrlBuilder.buildRunUrl(pipeline.organization, pipeline.fullName, branchName, nextRunId, 'pipeline') : '';
        const prevRunUrl = prevRunId ? UrlBuilder.buildRunUrl(pipeline.organization, pipeline.fullName, branchName, prevRunId, 'pipeline') : '';

        return (
            <span className="run-nav-container">
                {prevRunUrl && (
                    <Link to={prevRunUrl} title={t('rundetail.header.prev_run', { defaultValue: 'Previous Run' })}>
                        <Icon size={24} icon="HardwareKeyboardArrowLeft" style={{ verticalAlign: 'bottom' }} />
                    </Link>
                )}
                <RunIdCell run={run} />
                {nextRunUrl && (
                    <Link to={nextRunUrl} title={t('rundetail.header.next_run', { defaultValue: 'Next Run' })}>
                        <Icon size={24} icon="HardwareKeyboardArrowRight" style={{ verticalAlign: 'bottom' }} />
                    </Link>
                )}
            </span>
        );
    }
}

RunIdNavigation.propTypes = {
    run: PropTypes.object,
    pipeline: PropTypes.object,
    branchName: PropTypes.string,
    t: PropTypes.func,
};
