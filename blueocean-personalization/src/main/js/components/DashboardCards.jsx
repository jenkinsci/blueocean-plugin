/**
 * Created by cmeyers on 7/6/16.
 */
import React, { Component, PropTypes } from 'react';
import TransitionGroup from 'react-addons-css-transition-group';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';
import { List } from 'immutable';

import { favoritesSelector } from '../redux/FavoritesStore';
import { actions } from '../redux/FavoritesActions';
import favoritesSseListener from '../model/FavoritesSseListener';

import FavoritesProvider from './FavoritesProvider';
import { PipelineCard } from './PipelineCard';

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

    _onFavoriteToggle(isFavorite, favorite) {
        this.props.toggleFavorite(isFavorite, favorite.item, favorite);
    }

    _handleJobRunUpdate(runData) {
        this.props.updateRun(runData);
    }

    render() {
        if (!this.props.favorites) {
            return null;
        }

        const favoriteCards = this.props.favorites.map(favorite => {
            const pipeline = favorite.item;

            return (
                <div key={favorite._links.self.href}>
                    <PipelineCard
                      router={this.props.router}
                      runnable={pipeline}
                      favorite
                      onFavoriteToggle={(isFavorite) => this._onFavoriteToggle(isFavorite, favorite)}
                    />
                </div>
            );
        });

        return (
            <FavoritesProvider store={this.props.store}>
                <div className="favorites-card-stack">
                    <TransitionGroup transitionName="vertical-expand-collapse"
                      transitionEnterTimeout={300}
                      transitionLeaveTimeout={300}
                    >
                        {favoriteCards}
                    </TransitionGroup>
                </div>
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
    updateRun: PropTypes.func,
};

const selectors = createSelector(
    [favoritesSelector],
    (favorites) => ({ favorites })
);

export default connect(selectors, actions)(DashboardCards);
