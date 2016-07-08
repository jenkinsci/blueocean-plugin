/**
 * Created by cmeyers on 7/6/16.
 */
import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';

import { userSelector, favoritesSelector } from '../redux/FavoritesStore';
import { actions } from '../redux/FavoritesActions';

import { PipelineCard } from './PipelineCard';


/**
 */
export default class DashboardCards extends Component {

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
            const branch = fav.data;

            return (
                <div key={branch.fullName}>
                    <PipelineCard
                      organization={branch.organization}
                      pipeline={branch.fullName}
                      status={branch.latestRun.result}
                      branch={branch.name}
                      commitId={branch.commitId}
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
    favorites: PropTypes.array,
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
