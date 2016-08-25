/**
 * Created by cmeyers on 7/6/16.
 */
import React, { Component, PropTypes } from 'react';
import TransitionGroup from 'react-addons-css-transition-group';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';
import { List } from 'immutable';

import { classMetadataStore } from '@jenkins-cd/js-extensions';
import { ToastService as toastService } from '@jenkins-cd/blueocean-core-js';

import { favoritesSelector } from '../redux/FavoritesStore';
import { actions } from '../redux/FavoritesActions';
import favoritesSseListener from '../model/FavoritesSseListener';
import { uriEncodeOnce } from '../util/UrlUtils';

import FavoritesProvider from './FavoritesProvider';
import { PipelineCard } from './PipelineCard';

// the order the cards should be displayed based on their result/state (aka 'status')
const statusSortOrder = [
    'UNKNOWN', 'FAILURE', 'ABORTED', 'NOT_BUILT',
    'UNSTABLE', 'RUNNING', 'QUEUED', 'SUCCESS',
];

const extractStatus = (favorite) => {
    const latestRun = favorite && favorite.item && favorite.item.latestRun || {};
    return latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result;
};

// sorts the cards based on 1. status 2. endTime, startTime or enQueueTime (descending)
const sortComparator = (favoriteA, favoriteB) => {
    const statusA = extractStatus(favoriteA);
    const statusB = extractStatus(favoriteB);
    const orderA = statusSortOrder.indexOf(statusA);
    const orderB = statusSortOrder.indexOf(statusB);

    if (orderA < orderB) {
        return -1;
    } else if (orderA > orderB) {
        return 1;
    }

    const endTimeA = favoriteA && favoriteA.item && favoriteA.item.latestRun && favoriteA.item.latestRun.endTime;
    const endTimeB = favoriteB && favoriteB.item && favoriteB.item.latestRun && favoriteB.item.latestRun.endTime;

    if (endTimeA && endTimeB) {
        const endCompare = endTimeA.localeCompare(endTimeB);

        if (endCompare !== 0) {
            return -endCompare;
        }
    }

    const startTimeA = favoriteA && favoriteA.item && favoriteA.item.latestRun && favoriteA.item.latestRun.startTime;
    const startTimeB = favoriteB && favoriteB.item && favoriteB.item.latestRun && favoriteB.item.latestRun.startTime;

    if (startTimeA && startTimeB) {
        const startCompare = startTimeA.localeCompare(startTimeB);

        if (startCompare !== 0) {
            return -startCompare;
        }
    }

    const queuedTimeA = favoriteA && favoriteA.item && favoriteA.item.latestRun && favoriteA.item.latestRun.enQueueTime;
    const queuedTimeB = favoriteB && favoriteB.item && favoriteB.item.latestRun && favoriteB.item.latestRun.enQueueTime;

    if (queuedTimeA && queuedTimeB) {
        const queueCompare = queuedTimeA.localeCompare(queuedTimeB);

        if (queueCompare !== 0) {
            return -queueCompare;
        }
    }

    return 0;
};

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
            capabilities: {},
        };
    }

    componentWillMount() {
        favoritesSseListener.initialize(
            this.props.store,
            (runData, event) => this._handleJobRunUpdate(runData, event),
        );

        this._initializeCapabilities(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._initializeCapabilities(nextProps);
    }

    // TODO: eliminate capabilities code after JENKINS-37519 is implemented
    _initializeCapabilities(props) {
        if (props.favorites && props.favorites.size) {
            const capabilities = this.state.capabilities;

            for (const favorite of props.favorites) {
                const className = favorite.item._class;
                capabilities[className] = null;
                classMetadataStore.getClassMetadata(className, (classMeta) => this._updateCapabilities(classMeta));
            }

            this.setState({
                capabilities,
            });
        }
    }

    _updateCapabilities(classMeta) {
        const capabilities = this.state.capabilities;
        const className = classMeta.classes[0];
        if (!capabilities[className]) {
            capabilities[className] = classMeta;

            this.setState({
                capabilities,
            });
        }
    }

    _hasPendingCapabilities() {
        for (const key in this.state.capabilities) {
            if (!this.state.capabilities[key]) {
                return true;
            }
        }

        return false;
    }

    _getCapabilities(item) {
        const capabilities = this.state.capabilities[item._class];
        return capabilities && capabilities.classes ?
            capabilities.classes : [];
    }

    _hasCapability(item, capabilityName) {
        const capabilities = this._getCapabilities(item);
        return capabilities.indexOf(capabilityName) !== -1;
    }

    _onRunAgainClick(pipeline) {
        this.props.replayPipeline(pipeline);
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

    _handleJobRunUpdate(runData, event) {
        this.props.updateRun(runData);

        const name = decodeURIComponent(
            event.job_ismultibranch ? event.blueocean_job_branch_name : event.blueocean_job_pipeline_name
        );
        const runId = event.jenkins_object_id;

        if (event.jenkins_event === 'job_run_started') {
            const item = this._getFavoritedItem(event.blueocean_job_rest_url);
            const runDetailsUrl = this._buildRunDetailsUrl(item, runData);

            toastService.newToast({
                text: `Started "${name}" #${runId}`,
                action: 'Open',
                onActionClick: () => {
                    this.props.router.push({
                        pathname: runDetailsUrl,
                    });
                },
            });
        } else if (event.jenkins_event === 'job_run_ended' && runData.result === 'ABORTED') {
            toastService.newToast({
                text: `Stopped "${name}" #${runId}`,
            });
        }
    }

    _buildRunDetailsUrl(pipeline, run) {
        const names = this._extractNames(pipeline);
        const detailPart = names.branchName || names.pipelineName;
        return `/organizations/${uriEncodeOnce(pipeline.organization)}/` +
            `${uriEncodeOnce(names.fullName)}/detail/` +
            `${uriEncodeOnce(detailPart)}/${uriEncodeOnce(run.id)}/pipeline`;
    }

    _getFavoritedItem(itemUrl) {
        if (this.props.favorites) {
            const favorite = this.props.favorites.find(fav => fav.item._links.self.href === itemUrl);

            if (favorite) {
                return favorite.item;
            }
        }

        return null;
    }

    /**
     * Takes a pipeline/branch object and returns the fullName, pipelineName and branchName components
     * @param pipeline
     * @returns {{pipelineName: string, fullName: string, branchName: string}}
     * @private
     */
    _extractNames(pipeline) {
        const isBranch = this._hasCapability(pipeline, BRANCH_CAPABILITY);

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
        if (!this.props.favorites || this._hasPendingCapabilities()) {
            return null;
        }

        const sortedFavorites = this.props.favorites.sort(sortComparator);

        const favoriteCards = sortedFavorites.map(favorite => {
            const pipeline = favorite.item;
            const latestRun = pipeline.latestRun;
            const capabilities = this._getCapabilities(pipeline);
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
            }

            if (latestRun && latestRun.result) {
                status = latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result;
            }

            return (
                <div key={favorite._links.self.href}>
                    <PipelineCard
                      router={this.props.router}
                      item={pipeline}
                      capabilities={capabilities.classes}
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
