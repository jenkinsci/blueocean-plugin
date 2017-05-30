import React, {Component, PropTypes} from 'react';
import {
    CommitHash,
    ReadableDate,
    WeatherIcon,
    TableRow,
    TableCell,
} from '@jenkins-cd/design-language';
import {LiveStatusIndicator, RunButton} from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import {observer} from 'mobx-react';
import {Icon} from '@jenkins-cd/react-material-icons';
import {buildPipelineUrl} from '../util/UrlUtils';
import {Link} from 'react-router';
import RunMessageCell from './RunMessageCell';

import {buildRunDetailsUrl} from '../util/UrlUtils';

// For sorting the extensions in the actions column
function sortByOrdinal(extensions, done) {
    const sorted = extensions.sort((a, b) => {
        if (a.ordinal || b.ordinal) {
            if (!a.ordinal) return 1;
            if (!b.ordinal) return -1;
            if (a.ordinal < b.ordinal) return -1;
            return 1;
        }
        return a.pluginId.localeCompare(b.pluginId);
    });
    done(sorted);
}

// Intercept click events so they don't bubble back to containing components
function cancelClick(e) {
    // TODO: Find other things doing the same and merge this
    e.stopPropagation();
    e.preventDefault();
}

function noRun(branch, openRunDetails, t, store, columns) {
    const cleanBranchName = decodeURIComponent(branch.name);
    const statusIndicator = <LiveStatusIndicator result="NOT_BUILT"/>;
    const actions = [(
        <RunButton className="icon-button"
                   runnable={branch}
                   onNavigation={openRunDetails}
        />
    ), (
        <Extensions.Renderer extensionPoint="jenkins.pipeline.branches.list.action"
                             filter={sortByOrdinal}
                             pipeline={branch }
                             store={store}
                             {...t}
        />
    )];

    return (
        <BranchDetailsRowRenderer columns={columns}
                                  branchName={cleanBranchName}
                                  statusIndicator={statusIndicator}
                                  actions={actions}
        />
    );
}

export class BranchDetailsRowRenderer extends Component {
    render() {

        const {
            branchName,
            runDetailsUrl,
            weatherScore,
            statusIndicator,
            commitId,
            runMessage,
            completed,
            actions = [],
            latestRunId,
            ...restProps,
        } = this.props;

        const dataProps = {
            'data-branch': branchName,
        };

        if (latestRunId != null) {
            dataProps['data-runid'] = latestRunId;
        }

        const actionsCell = React.createElement(
            TableCell,
            {
                className: "TableCell--actions",
                onClick: cancelClick,
            },
            ...actions);

        return (
            <TableRow linkTo={runDetailsUrl} {...dataProps} {...restProps}>
                <TableCell>
                    { weatherScore != null && (
                        <WeatherIcon score={weatherScore}/>
                    )}
                </TableCell>
                <TableCell>{ statusIndicator }</TableCell>
                <TableCell>{ branchName }</TableCell>
                <TableCell><CommitHash commitId={commitId}/></TableCell>
                <TableCell>{ runMessage }</TableCell>
                <TableCell>{ completed }</TableCell>
                { actionsCell }
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
        const latestRun = branch.latestRun;
        if (!latestRun) {
            return noRun(branch, openRunDetails, t, this.context.store, columns);
        }
        const cleanBranchName = decodeURIComponent(branch.name);
        const runDetailsUrl = buildRunDetailsUrl(branch.organization, pipeline.fullName, cleanBranchName, latestRun.id, 'pipeline');
        const historyButtonUrl = `${buildPipelineUrl(branch.organization, pipeline.fullName)}/activity?branch=${encodeURIComponent(branch.name)}`;

        const statusIndicator = (
            <LiveStatusIndicator durationInMillis={latestRun.durationInMillis}
                                 result={latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result}
                                 startTime={latestRun.startTime}
                                 estimatedDuration={latestRun.estimatedDurationInMillis}
            />
        );

        const runMessage = (
            <RunMessageCell run={latestRun} t={t}/>
        );

        const completed = (
            <ReadableDate date={latestRun.endTime}
                          liveUpdate
                          locale={locale}
                          shortFormat={t('common.date.readable.short', {defaultValue: 'MMM DD h:mma Z'})}
                          longFormat={t('common.date.readable.long', {defaultValue: 'MMM DD YYYY h:mma Z'})}
            />
        );

        const actions = [
            (
                <RunButton
                    className="icon-button"
                    runnable={branch}
                    latestRun={branch.latestRun}
                    onNavigation={openRunDetails}
                />
            ), (
                <div className="history-button-component">
                    <Link to={historyButtonUrl} className="materials-icons history-button">
                        <Icon size={24} icon="history"/>
                    </Link>
                </div>
            ), (
                <Extensions.Renderer
                    extensionPoint="jenkins.pipeline.branches.list.action"
                    filter={sortByOrdinal}
                    pipeline={branch }
                    store={this.context.store}
                    {...t}
                />
            )];

        return (
            <BranchDetailsRowRenderer columns={columns}
                                      runDetailsUrl={runDetailsUrl}
                                      branchName={cleanBranchName}
                                      weatherScore={branch.weatherScore}
                                      statusIndicator={statusIndicator}
                                      commitId={latestRun.commitId}
                                      runMessage={runMessage}
                                      completed={completed}
                                      actions={actions}
                                      latestRunId={latestRun.id}
            />
        );


        // return (
        //     <CellRow linkUrl={runDetailsUrl} id={`${cleanBranchName}-${latestRun.id}`}>
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
