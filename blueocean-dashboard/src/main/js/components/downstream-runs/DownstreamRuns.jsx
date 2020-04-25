import React, { Component, PropTypes } from 'react';
import { observer } from 'mobx-react';
import { DownstreamRunsView } from './DownstreamRunsView';

/**
 * DownstreamRuns component to be used in-container. Takes a list of urls and descriptions from
 * run actions, coordinates with activityService (from context) to fetch details if necessary,
 * and hands the details off to DownstreamRunsView for rendering.
 */
@observer
export class DownstreamRuns extends Component {
    render() {
        const { runs = [], ...restProps } = this.props;
        const { activityService } = this.context;

        if (runs.length === 0) {
            // Nothing to see here, folks
            return null;
        }

        if (!activityService) {
            // Don't crash out, but don't fail silently either.
            console.error('DownstreamRuns: Missing context.activityService');
            return null;
        }

        const runsWithDetails = runs.map(({ runLink, runDescription }) => {
            const runDetails = activityService.getItem(runLink);

            if (!runDetails) {
                // We fetch any runs we don't already have stored, and when we setItem(result) mobX will re-render us
                activityService.fetchActivity(runLink, { useCache: true }).then(result => activityService.setItem(result));
            }
            return { runLink, runDescription, runDetails };
        });

        return <DownstreamRunsView runs={runsWithDetails} {...restProps} />;
    }
}

DownstreamRuns.contextTypes = {
    activityService: PropTypes.object.isRequired,
};

DownstreamRuns.propTypes = {
    runs: PropTypes.arrayOf(
        PropTypes.shape({
            runLink: PropTypes.string.isRequired,
            runDescription: PropTypes.string.isRequired,
        })
    ),
    locale: PropTypes.string,
    t: PropTypes.func,
};
