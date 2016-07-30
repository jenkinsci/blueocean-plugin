/**
 * Created by cmeyers on 7/20/16.
 */
import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';
import { List } from 'immutable';

import { userSelector, favoritesSelector } from '../redux/FavoritesStore';
import { actions } from '../redux/FavoritesActions';

import favoritesSseListener from '../model/FavoritesSseListener';

/**
 * FavoritesProvider ensures that the current user's favorites
 * are loaded for any components which may need it.
 *
 * Components that require this data can simply wrap themselves in
 * FavoritesProvider which will ensure the store is updated correctly.
 */
export class FavoritesProvider extends Component {

    componentWillMount() {
        this._initialize(this.props);
        favoritesSseListener.initialize(this.props.updateRun);
    }

    componentWillReceiveProps(props) {
        this._initialize(props);
    }

    componentWillUnmount() {
        favoritesSseListener.dispose();
    }

    _initialize(props) {
        const { user, favorites } = props;

        const shouldFetchUser = !user;
        const shouldFetchFavorites = user && !favorites;

        if (shouldFetchUser) {
            this.props.fetchUser();
        }

        if (shouldFetchFavorites) {
            this.props.fetchFavorites(user);
        }
    }

    render() {
        return this.props.children ?
            React.cloneElement(this.props.children, { ...this.props }) :
            null;
    }
}

FavoritesProvider.propTypes = {
    children: PropTypes.node,
    user: PropTypes.object,
    favorites: PropTypes.instanceOf(List),
    fetchUser: PropTypes.func,
    fetchFavorites: PropTypes.func,
    updateRun: PropTypes.func,
};

const selectors = createSelector(
    [userSelector, favoritesSelector],
    (user, favorites) => ({ user, favorites })
);

export default connect(selectors, actions)(FavoritesProvider);
