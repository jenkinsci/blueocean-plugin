import React, {Component, PropTypes} from 'react';

/**
 * Displays a commit hash in the proper style.
 */
export default class CommitHash extends Component {
    constructor() {
        super();
    }

    render() {
        if (this.props.changeset && this.props.changeset.commitId) {
            return (
                <code className="hash">{this.props.changeset.commitId.substring(0, 8)}</code>
            );
        }
        return (
            <span>-</span>
        );
    }
}

CommitHash.propTypes = {
    changeset: PropTypes.object.isRequired,
};
