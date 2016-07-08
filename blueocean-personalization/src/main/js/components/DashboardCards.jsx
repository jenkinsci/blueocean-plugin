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
    user: PropTypes.object,
    pipelines: PropTypes.array,
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
