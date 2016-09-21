/**
 * Created by cmeyers on 7/6/16.
 */
import React, { Component, PropTypes } from 'react';
import TransitionGroup from 'react-addons-css-transition-group';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';
import { List } from 'immutable';

import { capable, ToastService as toastService } from '@jenkins-cd/blueocean-core-js';

import { favoritesSelector } from '../redux/FavoritesStore';
import { actions } from '../redux/FavoritesActions';
import favoritesSseListener from '../model/FavoritesSseListener';
import { uriEncodeOnce } from '../util/UrlUtils';

import FavoritesProvider from './FavoritesProvider';
import { PipelineCard } from './PipelineCard';

/**
 * Extract elements from a path string deliminted with forward slashes
 * @param path
 * @param begin
 * @param end
 * @returns {string}
 */
const extractPath = (path, begin, end) => {
    try {
        return path.split('/').slice(begin, end).join('/');
    } catch (error) {
        return path;
    }
};

const BRANCH_CAPABILITY = 'io.jenkins.blueocean.rest.model.BlueBranch';

/**
 * Renders a stack of "favorites cards" including current most recent status.
 */
export class DashboardCards extends Component {

    constructor() {
        super();
        this.state = {
        };
    }

    componentWillMount() {
        favoritesSseListener.initialize(
            this.props.store,
            (runData, event) => this._handleJobRunUpdate(runData, event),
        );

        if (this.props.sortFavorites) {
            this.props.sortFavorites();
        }
    }

    _onRunAgainClick(pipeline) {
        this.props.replayPipeline(pipeline);

        const name = decodeURIComponent(pipeline.name);

        toastService.newToast({
            text: `Queued "${name}"`,
        });
    }

    _onRunClick(pipeline) {
        this.props.runPipeline(pipeline);

        const name = decodeURIComponent(pipeline.name);

        toastService.newToast({
            text: `Queued "${name}"`,
        });
    }

    _onStopClick(pipeline) {
        this.props.stopPipeline(pipeline);

        const name = decodeURIComponent(pipeline.name);
        const runId = pipeline.latestRun.id;

        toastService.newToast({
            text: `Stopping "${name}" #${runId}...`,
        });
    }

    _onFavoriteToggle(isFavorite, favorite) {
        this.props.toggleFavorite(isFavorite, favorite.item, favorite);
    }

    _handleJobRunUpdate(runData) {
        this.props.updateRun(runData);
    }

    /**
     * Takes a pipeline/branch object and returns the fullName, pipelineName and branchName components
     * @param pipeline
     * @returns {{pipelineName: string, fullName: string, branchName: string}}
     * @private
     */
    _extractNames(pipeline) {
        const isBranch = capable(pipeline, BRANCH_CAPABILITY);

        let fullName = null;
        let pipelineName = null;
        let branchName = null;

        if (isBranch) {
            // pipeline.fullName is in the form folder1/folder2/pipeline/branch ...
            // extract "pipeline"
            pipelineName = extractPath(pipeline.fullName, -2, -1);
            // extract everything up to "branch"
            fullName = extractPath(pipeline.fullName, 0, -1);
            branchName = pipeline.name;
        } else {
            pipelineName = pipeline.name;
            fullName = pipeline.fullName;
        }

        return {
            pipelineName, fullName, branchName,
        };
    }

    _renderCardStack() {
        if (!this.props.favorites) {
            return null;
        }

        const favoriteCards = this.props.favorites.map(favorite => {
            const pipeline = favorite.item;
            const latestRun = pipeline.latestRun;
            const names = this._extractNames(pipeline);

            let status = null;
            let startTime = null;
            let estimatedDuration = null;
            let commitId = null;
            let runId = null;

            if (latestRun) {
                if (latestRun.result) {
                    status = latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result;
                }

                startTime = latestRun.startTime;
                estimatedDuration = latestRun.estimatedDurationInMillis;
                commitId = latestRun.commitId;
                runId = latestRun.id;
            } else {
                status = 'NOT_BUILT';
            }

            return (
                <div key={favorite._links.self.href}>
                    <PipelineCard
                      router={this.props.router}
                      item={pipeline}
                      latestRun={latestRun}
                      status={status}
                      startTime={startTime}
                      estimatedDuration={estimatedDuration}
                      organization={pipeline.organization}
                      fullName={names.fullName}
                      pipeline={names.pipelineName}
                      branch={names.branchName}
                      commitId={commitId}
                      runId={runId}
                      favorite
                      onRunAgainClick={(pipeline1) => this._onRunAgainClick(pipeline1)}
                      onRunClick={(pipeline2) => this._onRunClick(pipeline2)}
                      onStopClick={(pipeline3) => this._onStopClick(pipeline3)}
                      onFavoriteToggle={(isFavorite) => this._onFavoriteToggle(isFavorite, favorite)}
                    />
                </div>
            );
        });

        return (
            <div className="favorites-card-stack">
                <TransitionGroup transitionName="vertical-expand-collapse"
                  transitionEnterTimeout={300}
                  transitionLeaveTimeout={300}
                >
                    {favoriteCards}
                </TransitionGroup>
            </div>
        );
    }

    render() {
        return (
            <FavoritesProvider store={this.props.store}>
                { this._renderCardStack() }
            </FavoritesProvider>
        );
    }
}

DashboardCards.propTypes = {
    store: PropTypes.object,
    router: PropTypes.object,
    favorites: PropTypes.instanceOf(List),
    sortFavorites: PropTypes.func,
    toggleFavorite: PropTypes.func,
    runPipeline: PropTypes.func,
    replayPipeline: PropTypes.func,
    stopPipeline: PropTypes.func,
    updateRun: PropTypes.func,
};

const selectors = createSelector(
    [favoritesSelector],
    (favorites) => ({ favorites })
);

export default connect(selectors, actions)(DashboardCards);
