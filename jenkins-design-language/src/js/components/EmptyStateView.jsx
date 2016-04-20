import React, {Component, PropTypes} from 'react';
import {EmptyStateIcon} from './EmptyStateIcon';

/**
 * Displays an "empty state" dialog with arbitrary child content and an optional icon.
 *
 * Content in the follow form will recent nice default styles:
 *
 * <h1>Title<h1>
 * <p>A longer message...</p>
 * <button>Take Action!</button> *
 *
 * Attribute: iconName="branch|goat|shoes"
 */
export class EmptyStateView extends Component {
    constructor() {
        super();
    }

    render() {
        return (
            <div className="empty-state">
                { this.props.iconName ? <EmptyStateIcon name={this.props.iconName} /> : null }
                <div className="empty-state-content">{this.props.children}</div>
            </div>
        );
    }
}

EmptyStateView.propTypes = {
    iconName: PropTypes.oneOf(['branch','goat','shoes']),
};
