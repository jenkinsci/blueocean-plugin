/**
 * Created by cmeyers on 7/8/16.
 */
import React, { Component, PropTypes } from 'react';
import { observer } from 'mobx-react';

import { Favorite } from '@jenkins-cd/design-language';
import { capable } from '@jenkins-cd/blueocean-core-js';

import favoriteStore from '../model/FavoriteStore';

/**
 * A toggle button to favorite or unfavorite the provided item (pipeline or branch)
 * Contains all logic for rendering the current favorite status of that item
 * and toggling favorited state on the server.
 */
@observer
export class FavoritePipeline extends Component {
    _onFavoriteToggle() {
        favoriteStore.setFavorite(this.props.pipeline, !favoriteStore.isFavorite(this.props.pipeline));
    }

    render() {
        // TODO: this should probably key off a more generic capability like 'FavoritableItem'
        if (capable(this.props.pipeline, 'hudson.matrix.MatrixProject')) {
            return null;
        }

        return (
            !(this.props.pipeline.branchNames && !this.props.pipeline.branchNames.length) && (
                <Favorite checked={favoriteStore.isFavorite(this.props.pipeline)} className={this.props.className} onToggle={() => this._onFavoriteToggle()} />
            )
        );
    }
}

FavoritePipeline.propTypes = {
    className: PropTypes.string,
    pipeline: PropTypes.object,
};

export default FavoritePipeline;
