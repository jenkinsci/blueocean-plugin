import React, { Component } from 'react';
import FavoritePipeline from './FavoritePipeline';

/**
 * Restyled version of FavoritePipeline component.
 *
 * Created by cmeyers on 7/20/16.
 */
export class FavoritePipelineHeader extends Component {

    render() {
        return (
            <FavoritePipeline { ...this.props } className="dark" />
        );
    }
}

export default FavoritePipelineHeader;
