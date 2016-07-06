/**
 * Created by cmeyers on 7/6/16.
 */
import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';

import { favoritesSelector } from '../redux/FavoritesStore';
import { actions } from '../redux/FavoritesActions';

import { PipelineCard } from './PipelineCard';


/**
 */
export default class DashboardCards extends Component {

    componentWillMount() {
        const config = this.context.config;

        if (config) {
            this.props.fetchFavorites(config);
        }
    }

    render() {
        if (!this.props.pipelines || !this.props.favorites) {
            return null;
        }

        const favoriteCards = this.props.favorites.map(fav => {
            const pipeline = this.props.pipelines.find((pipeline1) => {
                const fullName = `/organizations/${pipeline1.organization}/pipelines/${pipeline1.fullName}`;
                return fav.pipeline === fullName;
            });

            if (!pipeline) {
                return null;
            }

            return (
                <div key={fav.pipeline}>
                    <PipelineCard
                      organization={pipeline.organization}
                      pipeline={pipeline.fullName}
                      branch={'a'}
                      commitId={'b'}
                      favorite
                    />
                </div>
            );
        });

        return (
            <div>
                {favoriteCards}
            </div>
        );
    }
}

DashboardCards.propTypes = {
    pipelines: PropTypes.array,
    favorites: PropTypes.array,
    fetchFavorites: PropTypes.func,
};

DashboardCards.contextTypes = {
    config: PropTypes.object,
};

const selectors = createSelector([favoritesSelector], (favorites) => ({ favorites }));
export default connect(selectors, actions)(DashboardCards);
