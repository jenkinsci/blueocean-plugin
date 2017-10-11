// @flow

import React, { Component, PropTypes } from 'react';

const GIT_HASH_REGEX = /\b[0-9a-f]{5,40}\b/;

/**
 * Displays a commit hash in the proper style.
 */
export class CommitId extends Component {
    render() {
        const { commitId, url, title } = this.props;
        let displayValue;
        if (GIT_HASH_REGEX.test(commitId)) {
            displayValue = commitId.substring(0, 7);
        } else if (commitId) {
            displayValue = commitId;
        } else {
            displayValue = '—';
        }

        if (url) {
            return (
                <a href={url} target="_blank" title={title ? title : 'Opens commit in a new window'}>
                    <code className="hash">{displayValue}</code>
                </a>
            );
        }

        return <code className="hash">{displayValue}</code>;
    }
}

CommitId.propTypes = {
    commitId: PropTypes.string,
    url: PropTypes.string,
    title: PropTypes.string,
};
