// @flow

import React, {Component, PropTypes} from 'react';
import {EmptyStateIcon} from './EmptyStateIcon';
import { Icon } from './Icon';

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
 * iconName="branch|goat|shoes|<any-Icon-type>"
 * tightSpacing={true|false}
 */
export class EmptyStateView extends Component {
    render() {
        let layoutClasses = 'empty-state-container';

        if (this.props.tightSpacing) {
            layoutClasses = `${layoutClasses} tight-spacing`;
        }

        let icon = null;
        if (this.props.iconName) {
            if (['branch', 'goat', 'shoes'].indexOf(this.props.iconName) >= 0) {
                icon = <EmptyStateIcon name={this.props.iconName} />;
            } else {
                icon = (
                    <div className="empty-state-icon" style={{ paddingLeft: 0 }}>
                        <Icon size={150} icon={this.props.iconName} style={{fill: '#fff'}} />
                    </div>
                );
            }
        }

        return (
            <div className="empty-state">
                <div className={layoutClasses}>
                    { icon }
                    <div className="empty-state-content">{this.props.children}</div>
                </div>
            </div>
        );
    }
}

EmptyStateView.propTypes = {
    children: PropTypes.node,
    iconName: PropTypes.string,
    tightSpacing: PropTypes.bool,
};
