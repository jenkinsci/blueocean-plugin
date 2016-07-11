/**
 * Created by cmeyers on 7/8/16.
 */
import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';
import { List } from 'immutable';

import { Favorite } from '@jenkins-cd/design-language';

import { favoritesSelector } from '../redux/FavoritesStore';
import { actions } from '../redux/FavoritesActions';
import { checkMatchingFavoriteUrls } from '../util/FavoriteUtils';

/**
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

    _updateState(props) {
        const { pipeline } = props;
        let favorite = null;

        if (props.favorites) {
            favorite = props.favorites.find((fav) => {
                const favUrl = fav.item._links.self.href;
                const pipelineUrl = pipeline._links.self.href;
                
                return checkMatchingFavoriteUrls(favUrl, pipelineUrl);
            });
        }

        this.setState({
            favorite: !!favorite,
        });
    }

    _onFavoriteToggle() {
        const value = !this.state.favorite;
        this.setState({
            favorite: value,
        });

        if (this.props.toggleFavorite) {
            this.props.toggleFavorite(
                this.context.config,
                value,
                this.props.pipeline,
            );
        }
    }

    render() {
        return (
            <Favorite checked={this.state.favorite} className={this.props.className}
              onToggle={() => this._onFavoriteToggle()}
            />
        );
    }
}

FavoritePipeline.propTypes = {
    className: PropTypes.string,
    pipeline: PropTypes.object,
    favorites: PropTypes.instanceOf(List),
    toggleFavorite: PropTypes.func,
};

FavoritePipeline.defaultProps = {
    favorite: false,
};

FavoritePipeline.contextTypes = {
    config: PropTypes.object,
};

const selectors = createSelector(
    [favoritesSelector],
    (favorites) => ({ favorites })
);

export default connect(selectors, actions)(FavoritePipeline);
