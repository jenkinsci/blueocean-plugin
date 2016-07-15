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

        const favoriteCards = this.props.favorites.map(fav => {
            const branch = fav.item;
            const latestRun = branch.latestRun;

            let status = null;
            let startTime = null;
            let estimatedDuration = null;
            let commitId = null

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
                      pipeline={branch.fullName}
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
