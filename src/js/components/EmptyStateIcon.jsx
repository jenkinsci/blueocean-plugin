import React, {Component, PropTypes} from 'react';

/**
 * Displays one of the available "Noun Project" icons used in empty state panels
 */
export class EmptyStateIcon extends Component {
    constructor() {
        super();
    }

    render() {
        return (
            <div className="empty-state-icon">
                <img className={this.props.name} />
            </div>
        );
    }
}

EmptyStateIcon.propTypes = {
    name: PropTypes.oneOf(['branch','goat','shoes']).isRequired,
};
