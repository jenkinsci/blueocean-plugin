/**
 * Created by cmeyers on 7/6/16.
 */
import React, { Component, PropTypes } from 'react';
import { observer } from 'mobx-react';
import TransitionGroup from 'react-addons-css-transition-group';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

import favoriteStore from '../model/FavoriteStore';
import favoritesSseListener from '../model/FavoritesSseListener';

import { PipelineCard } from './PipelineCard';

const t = i18nTranslator('blueocean-personalization');

function CardStack(props) {
    const { children, message } = props;
    return (
        <div className="favorites-card-stack">
            <div className="favorites-card-stack-heading"> {message}</div>
            <TransitionGroup transitionName="vertical-expand-collapse" transitionEnterTimeout={300} transitionLeaveTimeout={300}>
                {children}
            </TransitionGroup>
        </div>
    );
}
CardStack.propTypes = {
    children: PropTypes.array,
    message: PropTypes.string,
};

/**
 * Renders a stack of "favorites cards" including current most recent status.
 */
@observer
export class DashboardCards extends Component {
    _onFavoriteToggle(item) {
        favoriteStore.setFavorite(item, !favoriteStore.isFavorite(item));
    }

    componentWillMount() {
        favoritesSseListener.initialize(favoriteStore.onPipelineRun);
    }

    componentWillUnmount() {
        favoritesSseListener.unsubscribe();
        favoriteStore.clearCache(); // since we're not listening for events anymore...
    }

    render() {
        const locale = t && t.lng;

        // empty array will be filled in the next method if any paused fav's exist
        const pausedCards = [];
        const favoriteCards = favoriteStore.favorites.map(favorite => {
            const pipeline = favorite.item;
            const responseElement = (
                <div key={favorite._links.self.href}>
                    <PipelineCard
                        router={this.props.router}
                        runnable={pipeline}
                        t={t}
                        locale={locale}
                        favorite
                        onFavoriteToggle={() => this._onFavoriteToggle(pipeline)}
                    />
                </div>
            );
            // if we are in paused state fill the pause array and return null
            if (favorite.item.latestRun && favorite.item.latestRun.state === 'PAUSED') {
                pausedCards.push(responseElement);
                return null;
            }
            return responseElement;
        });

        // Only show paused pipelines when we really have some
        // do we have any paused pipelines?
        const pausedCardsStack = pausedCards.length > 0 ? <CardStack message={t('dashboardCard.input.required')}>{pausedCards}</CardStack> : null;
        const favoriteCardsStack = favoriteCards.length > 0 ? <CardStack message={t('dashboardCard.input.favorite')}>{favoriteCards}</CardStack> : null;

        return (
            <div>
                {pausedCardsStack}
                {favoriteCardsStack}
            </div>
        );
    }
}

DashboardCards.propTypes = {
    router: PropTypes.object,
};

export default DashboardCards;
