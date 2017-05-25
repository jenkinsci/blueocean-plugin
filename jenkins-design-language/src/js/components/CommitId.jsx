// @flow

import React, {Component, PropTypes} from 'react';

const GIT_HASH_REGEX = /\b[0-9a-f]{5,40}\b/;

/**
 * Displays a commit hash in the proper style.
 */
export class CommitId extends Component {
    render() {
        const commitId = this.props.commitId;
        let displayValue;
        if (GIT_HASH_REGEX.test(commitId)) {
            displayValue = commitId.substring(0, 7);
        } else if (commitId) {
            displayValue = commitId;
        } else {
            displayValue = '–';
        }
        return (<code className="hash">{displayValue}</code>);
    }
}

CommitId.propTypes = {
    commitId: PropTypes.string,
};
