/**
 * Created by cmeyers on 7/6/16.
 */
import React, { Component, PropTypes } from 'react';
import TransitionGroup from 'react-addons-css-transition-group';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';
import { List } from 'immutable';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';


import { favoritesSelector } from '../redux/FavoritesStore';
import { actions } from '../redux/FavoritesActions';
import favoritesSseListener from '../model/FavoritesSseListener';

import FavoritesProvider from './FavoritesProvider';
import { PipelineCard } from './PipelineCard';

const t = i18nTranslator('blueocean-personalization');

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
        const favorites = this.props.favorites || [];

        const locale = t && t.lng;

        // empty array will be filled in the next method if any paused fav's exist
        const pausedCards = [];
        const favoriteCards = favorites.map(favorite => {
            const pipeline = favorite.item;
            const responseElement = (
                <div key={favorite._links.self.href}>
                    <PipelineCard
                      router={this.props.router}
                      runnable={pipeline}
                      t={t}
                      locale={locale}
                      favorite
                      onFavoriteToggle={(isFavorite) => this._onFavoriteToggle(isFavorite, favorite)}
                    />
                </div>
            );
            // if we are in paused state fill the pause array and return null
            if (favorite.item.latestRun && favorite.item.latestRun.state === 'PAUSED') {
                pausedCards.push(responseElement);
                return null;
            }
            return (responseElement);
        });

        // generic sub-render to output fav or paused stacks
        const StackOutput = (properties) => {
            const { cards, message } = properties;
            return (<div key={message} className="favorites-card-stack">
                <div className="favorites-card-stack-heading"> {message}</div>
                <TransitionGroup transitionName="vertical-expand-collapse"
                  transitionEnterTimeout={300}
                  transitionLeaveTimeout={300}
                >
                    {cards}
                </TransitionGroup>
            </div>);
        };
        // Only show paused pipelines when we really have some
        // do we have any paused pipelines?
        const pausedCardsStack = pausedCards.length > 0 ? (<StackOutput
          message={t('dashboardCard.input.required', { defaultValue: 'Input required' })}
          cards={pausedCards}
        />) : null;
        const favoriteCardsStack = favoriteCards.size > 0 ? (<StackOutput
          message={t('dashboardCard.input.favorite', { defaultValue: 'Favorites' })}
          cards={favoriteCards}
        />) : null;

        return (
            <FavoritesProvider store={this.props.store}>
                <div>
                    { pausedCardsStack }
                    { favoriteCardsStack }
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
