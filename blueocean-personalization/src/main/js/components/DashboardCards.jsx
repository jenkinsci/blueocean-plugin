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

    componentWillMount() {
        this._initialize();
    }

    componentWillReceiveProps() {
        this._initialize();
    }

    _initialize() {
        const config = this.context.config;
        const { user, favorites } = this.props;

        if (config) {
            if (!user) {
                this.props.fetchUser(config);
            } else if (!favorites) {
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

            const commitId = latestRun ? latestRun.commitId : null;

            let status = null;

            if (latestRun && latestRun.result) {
                status = latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result;
            }

            return (
                <div key={fav._links.self.href}>
                    <PipelineCard
                      status={status}
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
