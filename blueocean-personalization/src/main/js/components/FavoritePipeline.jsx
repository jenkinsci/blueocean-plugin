/**
 * Created by cmeyers on 7/8/16.
 */
import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';
import { List } from 'immutable';

import { Favorite } from '@jenkins-cd/design-language';
import { capable } from '@jenkins-cd/blueocean-core-js';

import { favoritesSelector } from '../redux/FavoritesStore';
import { actions } from '../redux/FavoritesActions';
import { checkMatchingFavoriteUrls } from '../util/FavoriteUtils';
import FavoritesProvider from './FavoritesProvider';

/**
 * A toggle button to favorite or unfavorite the provided item (pipeline or branch)
 * Contains all logic for rendering the current favorite status of that item
 * and toggling favorited state on the server.
 */
export class FavoritePipeline extends Component {

    constructor(props) {
        super(props);

        this.state = {
            favorite: false,
        };
    }

    componentWillMount() {
        this._updateState(this.props);
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.favorites !== nextProps.favorites) {
            this._updateState(nextProps);
        }
    }

    _findMatchingFavorite(pipeline, favorites) {
        if (!pipeline || !favorites) {
            return null;
        }

        return favorites.find((fav) => {
            const favUrl = fav.item._links.self.href;
            const pipelineUrl = pipeline._links.self.href;
            return checkMatchingFavoriteUrls(favUrl, pipelineUrl);
        });
    }

    _updateState(props) {
        const { pipeline } = props;
        const favorite = this._findMatchingFavorite(pipeline, props.favorites);

        this.setState({
            favorite: !!favorite,
        });
    }

    _onFavoriteToggle() {
        const isFavorite = !this.state.favorite;
        this.setState({
            favorite: isFavorite,
        });

        const favorite = this._findMatchingFavorite(this.props.pipeline, this.props.favorites);

        if (this.props.toggleFavorite) {
            this.props.toggleFavorite(isFavorite, this.props.pipeline, favorite);
        }
    }

    render() {
        // TODO: this should probably key off a more generic capability like 'FavoritableItem'
        if (capable(this.props.pipeline, 'hudson.matrix.MatrixProject')) {
            return null;
        }

        return (!(this.props.pipeline.branchNames && !this.props.pipeline.branchNames.length) ? (
            <FavoritesProvider store={this.props.store}>
                <Favorite checked={this.state.favorite} className={this.props.className}
                  onToggle={() => this._onFavoriteToggle()}
                />
            </FavoritesProvider>
        ) : null);
    }
}

FavoritePipeline.propTypes = {
    className: PropTypes.string,
    pipeline: PropTypes.object,
    favorites: PropTypes.instanceOf(List),
    toggleFavorite: PropTypes.func,
    store: PropTypes.object,
};

FavoritePipeline.defaultProps = {
    favorite: false,
};

const selectors = createSelector(
    [favoritesSelector],
    (favorites) => ({ favorites })
);

export default connect(selectors, actions)(FavoritePipeline);
