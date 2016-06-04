// @flow

import React, {Component, PropTypes} from 'react';

const HASH_REGEX = /\b[0-9a-f]{5,40}\b/;

/**
 * Displays a commit hash in the proper style.
 */
export class CommitHash extends Component {
    render() {
        if (HASH_REGEX.test(this.props.commitId)) {
            return (
                <code className="hash">{this.props.commitId.substring(0, 7)}</code>
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
