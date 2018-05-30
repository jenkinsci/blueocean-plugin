import React, { Component, PropTypes } from 'react';

import { JTable, TableCell, TableRow, PlaceholderIcon, PlaceholderText, LiveStatusIndicator, TimeDuration, ReadableDate } from '@jenkins-cd/design-language';

import { UrlBuilder } from '@jenkins-cd/blueocean-core-js';

//--------------------------------------------------------------------------
//  Downstream Runs component (renderer)
//
//  Logic for looking up details from actions lives in DownstreamRuns.jsx
//--------------------------------------------------------------------------

/**
 * Try to construct a sensible text identifier for a specific run
 */
function getRunIdentifier(runDetails) {
    let result = '';
    if (runDetails.organization && runDetails.organization !== 'jenkins') {
        result = runDetails.organization + ' / ';
    }
    result += runDetails.name || runDetails.pipeline || '' || runDetails.id;

    return result;
}

/**
 * Renders a list of downstream runs in a table, used for the pipeline view of a run.
 */
export class DownstreamRunsView extends Component {
    render() {
        const { runs = [], getTimes = x => x, locale, t } = this.props;

        if (!runs.length) {
            return null; // No placeholder for this, it shouldn't be rendered when there's no runs
        }

        const columns = [
            JTable.column(30, '', false), // Status
            JTable.column(60, '', false), // Run
            JTable.column(480, '', true), // Description
            JTable.column(150, '', false), // Duration
            JTable.column(100, '', false), // Completed
        ];

        const children = runs.map(run => {
            if (run.runDetails) {
                // We have populated info

                const runDetails = run.runDetails;
                const runResult = runDetails.result === 'UNKNOWN' ? runDetails.state : runDetails.result;
                const isRunning = runResult === 'RUNNING' || runResult === 'PAUSED' || runResult === 'QUEUED';

                const runDetailsUrl = UrlBuilder.buildRunUrlForDetails(runDetails);

                const { durationInMillis, estimatedDurationInMillis } = runDetails;

                const identifier = getRunIdentifier(runDetails);

                const { endTime } = getTimes({
                    result: runResult,
                    durationInMillis: durationInMillis,
                    startTime: runDetails.startTime,
                    endTime: runDetails.endTime,
                });

                return (
                    <TableRow key={run.runLink} linkTo={runDetailsUrl}>
                        <TableCell>
                            <LiveStatusIndicator
                                width="20px"
                                height="20px"
                                result={runResult}
                                durationInMillis={durationInMillis}
                                estimatedDuration={estimatedDurationInMillis}
                            />
                        </TableCell>
                        <TableCell>{runDetails.id}</TableCell>
                        <TableCell>
                            <span className="text-with-ellipsis-container">{identifier}</span>
                        </TableCell>
                        <TableCell>
                            <TimeDuration millis={durationInMillis} updatePeriod={1000} liveUpdate={isRunning} locale={locale} t={t} />
                        </TableCell>
                        <TableCell>
                            <ReadableDate
                                date={endTime}
                                liveUpdate
                                locale={locale}
                                shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                                longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
                            />
                        </TableCell>
                    </TableRow>
                );
            } else {
                // Waiting on details
                return (
                    <TableRow key={run.runLink}>
                        <TableCell>
                            <PlaceholderIcon size={20} />
                        </TableCell>
                        <TableCell>
                            <PlaceholderText size={55} />
                        </TableCell>
                        <TableCell>{run.runDescription}</TableCell>
                        <TableCell>
                            <PlaceholderText size={55} />
                        </TableCell>
                        <TableCell>
                            <PlaceholderText size={55} />
                        </TableCell>
                    </TableRow>
                );
            }
        });

        return (
            <div className="DownstreamRuns">
                <JTable columns={columns} className="DownstreamRuns-table" noMaxWidth>
                    {children}
                </JTable>
            </div>
        );
    }
}

// Fixme: we need these to exist canonically somewhere, with useful enums and such
const runDetailShape = PropTypes.shape({
    result: PropTypes.string,
    state: PropTypes.string,
    organization: PropTypes.string,
    pipeline: PropTypes.string,
    id: PropTypes.string,
    durationInMillis: PropTypes.number,
    estimatedDurationInMillis: PropTypes.number,
    startTime: PropTypes.string,
    endTime: PropTypes.string,
    name: PropTypes.string,
});

DownstreamRunsView.propTypes = {
    runs: PropTypes.arrayOf(
        PropTypes.shape({
            runLink: PropTypes.string.isRequired,
            runDescription: PropTypes.string.isRequired,
            runDetails: runDetailShape,
        })
    ),
    getTimes: PropTypes.func,
    locale: PropTypes.string,
    t: PropTypes.func,
};
