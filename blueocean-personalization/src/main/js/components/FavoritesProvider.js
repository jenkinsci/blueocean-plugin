/**
 * Created by cmeyers on 7/20/16.
 */
import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';
import { List } from 'immutable';

import { User } from '@jenkins-cd/blueocean-core-js';
import { favoritesSelector } from '../redux/FavoritesStore';
import { actions } from '../redux/FavoritesActions';

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
    }

    componentWillReceiveProps(props) {
        this._initialize(props);
    }

    _initialize(props) {
        const { favorites } = props;
        this.user = User.current();

        const shouldFetchFavorites = this.user && !this.user.isAnonymous() && !favorites;

        if (shouldFetchFavorites) {
            this.props.fetchFavorites(this.user);
        }
    }

    render() {
        if (this.user && !this.user.isAnonymous()) {
            if (this.props.children) {
                return React.cloneElement(this.props.children, { ...this.props });
            }
        }

        return null;
    }
}

FavoritesProvider.propTypes = {
    children: PropTypes.node,
    favorites: PropTypes.instanceOf(List),
    fetchFavorites: PropTypes.func,
};

const selectors = createSelector(
    [favoritesSelector],
    (favorites) => ({ favorites })
);

export default connect(selectors, actions)(FavoritesProvider);
