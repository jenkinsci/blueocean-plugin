import * as React from 'react';
import { Icon } from '@jenkins-cd/design-language';
import { UrlBuilder } from '@jenkins-cd/blueocean-core-js';
import { Link } from 'react-router';
import * as RunIdCell from './RunIdCell';

import { Model } from '@jenkins-cd/blueocean-core-js';

type Props = {
    run: Model.Run;
    pipeline: Model.RunnableItem;
    branchName: string;
    t: Function;
};

function getRunId(link?: Model.Link) {
    if (!link || !link.href) {
        return null;
    }
    const pattern = /[\/].*runs\/*([0-9]*)/g;
    const match = pattern.exec(link.href);
    return match && match[1];
}

export class RunIdNavigation extends React.Component {
    props: Props;

    render() {
        const { run, pipeline, branchName, t } = this.props;

        const nextRunId = getRunId(run._links.nextRun) || '';
        const prevRunId = getRunId(run._links.prevRun) || '';

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
