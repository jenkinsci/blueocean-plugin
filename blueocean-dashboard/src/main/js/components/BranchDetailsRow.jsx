import React, { Component, PropTypes } from 'react';
import {
    CommitHash,
    ReadableDate,
    WeatherIcon,
    TableRow,
    TableCell,
} from '@jenkins-cd/design-language';
import { LiveStatusIndicator, RunButton } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { observer } from 'mobx-react';
import { CellRow, CellLink } from './CellLink';
import { Icon } from '@jenkins-cd/react-material-icons';
import { buildPipelineUrl } from '../util/UrlUtils';
import { Link } from 'react-router';
import RunMessageCell from './RunMessageCell';

import { buildRunDetailsUrl } from '../util/UrlUtils';

// Intercept click events so they don't bubble back to containing components
function cancelClick(e) {
    // TODO: Find other things doing the same and merge this
    e.stopPropagation();
    e.preventDefault();
}

function noRun(branch, openRunDetails, t, store, columns) {
    return (
        <BranchDetailsRowRenderer columns={columns} />
    );
}

export class BranchDetailsRowRenderer extends Component {
    render() {

        const restProps = this.props;

        return (
            <TableRow {...restProps}>
                 <TableCell>X</TableCell>
                 <TableCell>X</TableCell>
                 <TableCell>X</TableCell>
                 <TableCell>X</TableCell>
                 <TableCell>X</TableCell>
                 <TableCell>X</TableCell>
                 <TableCell className="TableCell--actions" onClick={cancelClick}>X</TableCell>
            </TableRow>
        );
    }
}

@observer
export class BranchDetailsRow extends Component {

    render() {
        
        const {
            data: branch,
            pipeline,
            t,
            locale,
            columns
        } = this.props;

        // early out
        if (!branch || !pipeline) {
            return null;
        }

        // const { router, location } = this.context;
        const openRunDetails = (newUrl) => {
            location.pathname = newUrl;
            router.push(location);
        };
        // const latestRun = branch.latestRun;
        // if (!latestRun) {
            return noRun(branch, openRunDetails, t, this.context.store, columns);
        // }
        // const cleanBranchName = decodeURIComponent(branch.name);
        // const runDetailsUrl = buildRunDetailsUrl(branch.organization, pipeline.fullName, cleanBranchName, latestRun.id, 'pipeline');
        // const historyButtonUrl = `${buildPipelineUrl(branch.organization, pipeline.fullName)}/activity?branch=${encodeURIComponent(branch.name)}`;
        // return (
        //     <CellRow linkUrl={runDetailsUrl} id={`${cleanBranchName}-${latestRun.id}`}>
        //         <CellLink disableDefaultPadding>
        //             <WeatherIcon score={branch.weatherScore} />
        //         </CellLink>
        //         <CellLink>
        //             <LiveStatusIndicator
        //                 durationInMillis={latestRun.durationInMillis}
        //                 result={latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result}
        //                 startTime={latestRun.startTime}
        //                 estimatedDuration={latestRun.estimatedDurationInMillis}
        //             />
        //         </CellLink>
        //         <CellLink>{cleanBranchName}</CellLink>
        //         <CellLink><CommitHash commitId={latestRun.commitId} /></CellLink>
        //         <CellLink><RunMessageCell run={latestRun} t={t} /></CellLink>
        //         <CellLink>
        //             <ReadableDate
        //                 date={latestRun.endTime}
        //                 liveUpdate
        //                 locale={locale}
        //                 shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
        //                 longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
        //             />
        //         </CellLink>
        //         <td className="actions">
        //
        //             <RunButton
        //                 className="icon-button"
        //                 runnable={branch}
        //                 latestRun={branch.latestRun}
        //                 onNavigation={openRunDetails}
        //             />
        //
        //             <div className="history-button-component">
        //                 <Link to={historyButtonUrl} className="materials-icons history-button">
        //                     <Icon size={24} icon="history" />
        //                 </Link>
        //             </div>
        //
        //             <Extensions.Renderer
        //                 extensionPoint="jenkins.pipeline.branches.list.action"
        //                 filter={sortByOrdinal}
        //                 pipeline={branch }
        //                 store={this.context.store}
        //                 {...t}
        //             />
        //         </td>
        //     </CellRow>
        // );
    }
}

BranchDetailsRow.propTypes = {
    data: PropTypes.object.isRequired,
    t: PropTypes.func,
    locale: PropTypes.string,
    pipeline: PropTypes.object,
};

BranchDetailsRow.contextTypes = {
    store: PropTypes.object,
    router: PropTypes.object.isRequired, // From react-router
    location: PropTypes.object,
};
