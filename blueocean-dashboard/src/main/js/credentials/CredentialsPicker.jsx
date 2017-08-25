import React, { PropTypes } from 'react';

import GitCredentialsPicker from './git/GitCredentialsPicker';
import GithubCredentialsPicker from './github/GithubCredentialsPicker';
import BbCredentialsPicker from './bitbucket/BbCredentialsPicker';


/**
 * Provides UI and backend integration for acquiring a credential.
 * type: 'github', 'github-enterprise', 'bitbucket-cloud', 'bitbucket-server'
 * onStatus: function invoked with 'promptLoading', 'promptReady'
 * onComplete: function invoked with credential and 'autoSelected', 'userSelected'
 */
class CredentialsPicker extends React.Component {

    resolveType(props) {
        if (props.type) {
            return props.type;
        }
        if (props.scmSource && props.scmSource.id) {
            return props.scmSource.id;
        }
        return null;
    }

    resolveScmSource(props) {
        if (props.scmSource) {
            return props.scmSource;
        }
        if (props.githubConfig) {
            return {
                id: props.githubConfig.scmId,
                apiUrl: props.githubConfig.apiUrl,
            };
        }
        return {};
    }

    render() {
        const { onStatus, onComplete, dialog } = this.props;
        const type = this.resolveType(this.props);
        const scmSource = this.resolveScmSource(this.props);

        let children = null;

        if (type === 'github' || type === 'github-enterprise') {
            children = (
                <GithubCredentialsPicker
                    scmId={scmSource.id}
                    apiUrl={scmSource.apiUrl}
                />
            );
        } else if (type === 'bitbucket-cloud' || type === 'bitbucket-server') {
            children = (
                <BbCredentialsPicker
                    scmId={scmSource.id}
                    apiUrl={scmSource.apiUrl}
                />
            );
        } else if (type === 'git') {
            children = (
                <GitCredentialsPicker
                    scmId={scmSource.id}
                />
            );
        } else {
            children = <div>No credential picker could be found for type={type}</div>;
        }

        return (
            <div className="credentials-picker">
                { React.cloneElement(children, { onStatus, onComplete, dialog }) }
            </div>
        );
    }
}

CredentialsPicker.propTypes = {
    type: PropTypes.string,
    onStatus: PropTypes.func,
    onComplete: PropTypes.func,
    dialog: PropTypes.bool,
    scmSource: PropTypes.shape({
        id: PropTypes.string,
        apiUrl: PropTypes.string,
    }),
    githubConfig: PropTypes.shape({
        scmId: PropTypes.string,
        apiUrl: PropTypes.string,
    }),
};

export default CredentialsPicker;
