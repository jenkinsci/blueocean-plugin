import React, {Component, PropTypes} from 'react';

/**
 * Displays a commit hash in the proper style.
 */
export default class CommitHash extends Component {
    constructor() {
        super();
    }

    render() {
        if (this.props.commitId) {
            return (
                <code className="hash">{this.props.commitId.substring(0, 8)}</code>
            );
        }
        return (
            <span>-</span>
        );
    }
}

CommitHash.propTypes = {
    commitId: PropTypes.string,
};
