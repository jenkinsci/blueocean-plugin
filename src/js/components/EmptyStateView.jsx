// @flow

import React, {Component, PropTypes} from 'react';
import {EmptyStateIcon} from './EmptyStateIcon';

/**
 * Displays an "empty state" dialog with arbitrary child content and an optional icon.
 *
 * Content in the following form will recent nice default styles:
 *
 * <h1>Title<h1>
 * <p>A longer message...</p>
 * <button>Take Action!</button> *
 *
 * Properties:
 * iconName="branch|goat|shoes"
 * tightSpacing={true|false}
 */
export class EmptyStateView extends Component {
    render() {
        let layoutClasses = 'empty-state-container';

        if (this.props.tightSpacing) {
            layoutClasses = `${layoutClasses} tight-spacing`;
        }

        return (
            <div className="empty-state">
                <div className={layoutClasses}>
                    { this.props.iconName ? <EmptyStateIcon name={this.props.iconName} /> : null }
                    <div className="empty-state-content">{this.props.children}</div>
                </div>
            </div>
        );
    }
}

EmptyStateView.propTypes = {
    iconName: PropTypes.oneOf(['branch','goat','shoes']),
    tightSpacing: PropTypes.bool,
};
