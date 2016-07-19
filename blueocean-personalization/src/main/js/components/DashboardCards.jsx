/**
 * Created by cmeyers on 7/6/16.
 */
import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';
import { List } from 'immutable';

import { userSelector, favoritesSelector } from '../redux/FavoritesStore';
import { actions } from '../redux/FavoritesActions';

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
 */
export class DashboardCards extends Component {

    constructor(props) {
        super(props);

        this.fetchUserInProgress = false;
        this.fetchFavoritesInProgress = false;
    }

    componentWillMount() {
        this._initialize(this.props);
    }

    componentWillReceiveProps(props) {
        this._initialize(props);
    }

    _initialize(props) {
        const config = this.context.config;
        const { user, favorites } = props;

        if (user) {
            this.fetchUserInProgress = false;
        }

        if (favorites) {
            this.fetchFavoritesInProgress = false;
        }

        if (config) {
            const shouldFetchUser = !user && !this.fetchUserInProgress;
            const shouldFetchFavorites = user && !favorites && !this.fetchFavoritesInProgress;

            if (shouldFetchUser) {
                this.fetchUserInProgress = true;
                this.props.fetchUser(config);
            }

            if (shouldFetchFavorites) {
                this.fetchFavoritesInProgress = true;
                this.props.fetchFavorites(config, user);
            }
        }
    }

    render() {
        if (!this.props.favorites) {
            return null;
        }

        const sortedFavorites = this.props.favorites.sort(sortComparator);

        const favoriteCards = sortedFavorites.map(fav => {
            const branch = fav.item;
            const latestRun = branch.latestRun;
            // TODO: make this better
            const jobName = branch.fullName.split('/').slice(-2)[0];

            let status = null;
            let startTime = null;
            let estimatedDuration = null;
            let commitId = null;

            if (latestRun) {
                if (latestRun.result) {
                    status = latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result;
                }

                startTime = latestRun.startTime;
                estimatedDuration = latestRun.estimatedDurationInMillis;
                commitId = latestRun.commitId;
            }

            if (latestRun && latestRun.result) {
                status = latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result;
            }

            return (
                <div key={fav._links.self.href}>
                    <PipelineCard
                      status={status}
                      startTime={startTime}
                      estimatedDuration={estimatedDuration}
                      organization={branch.organization}
                      pipeline={jobName}
                      branch={branch.name}
                      commitId={commitId}
                      favorite
                    />
                </div>
            );
        });

        return (
            <div className="favorites-card-stack">
                {favoriteCards}
            </div>
        );
    }
}

DashboardCards.propTypes = {
    user: PropTypes.object,
    favorites: PropTypes.instanceOf(List),
    fetchUser: PropTypes.func,
    fetchFavorites: PropTypes.func,
};

DashboardCards.contextTypes = {
    config: PropTypes.object,
};

const selectors = createSelector(
    [userSelector, favoritesSelector],
    (user, favorites) => ({ user, favorites })
);

export default connect(selectors, actions)(DashboardCards);
