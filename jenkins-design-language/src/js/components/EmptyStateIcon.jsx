// @flow

import React, {Component, PropTypes} from 'react';

/**
 * Displays one of the available "Noun Project" icons used in empty state panels
 */
export class EmptyStateIcon extends Component {
    constructor() {
        super();
    }

    render() {
        const classNames = `empty-state-icon ${this.props.name}`;
        return (
            <div className={classNames}/>
        );
    }
}

EmptyStateIcon.propTypes = {
    name: PropTypes.oneOf(['branch','goat','shoes']).isRequired,
};
