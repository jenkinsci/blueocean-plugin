import React, { PropTypes } from 'react';
import { GitCredentialsPickerSSH } from './GitCredentialsPickerSSH';
import { GitCredentialsPickerPassword } from './GitCredentialsPickerPassword';

function isSshRepositoryUrl(url) {
    if (typeof url !== 'string' || url.trim().length === 0) {
        return false;
    }

    if (/^ssh:\/\/.*/.test(url)) {
        // is ssh:// protocol
        return true;
    }

    if (/^[^@:]+@.*/.test(url)) {
        // No protocol, but has a "user@host[...]" format
        return true;
    }

    return false;
}

/**
 * Just a wrapper to decide between the SSH component and username/pw component based on repositoryUrl
 */
class GitCredentialsPicker extends React.Component {
    render() {
        const { repositoryUrl } = this.props;

        if (!repositoryUrl) {
            return null; // Repo URL decides wether we show certificate or un/pw
        }

        if (isSshRepositoryUrl(this.props.repositoryUrl)) {
            return <GitCredentialsPickerSSH {...this.props} />;
        }

        return <GitCredentialsPickerPassword {...this.props} />;
    }
}

GitCredentialsPicker.propTypes = {
    onStatus: PropTypes.func,
    onComplete: PropTypes.func,
    requirePush: PropTypes.bool,
    branch: PropTypes.string,
    scmId: PropTypes.string,
    dialog: PropTypes.bool,
    repositoryUrl: PropTypes.string,
    pipeline: PropTypes.object,
};

GitCredentialsPicker.contextTypes = {
    router: React.PropTypes.object,
};

export default GitCredentialsPicker;
