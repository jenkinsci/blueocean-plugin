import React, { PropTypes } from 'react';

import GithubCredentialsPicker from './github/GithubCredentialsPicker';
import BbCredentialsPicker from './bitbucket/BbCredentialsPicker';


/**
 * Provides UI and backend integration for acquiring a credential.
 * type: 'github', 'github-enterprise', 'bitbucket-cloud', 'bitbucket-server'
 * onStatus: function invoked with 'promptLoading', 'promptReady'
 * onComplete: function invoked with credential and 'autoSelected', 'userSelected'
 */
class CredentialsPicker extends React.Component {

    render() {
        const { type, onStatus, onComplete } = this.props;
        let typedPicker = null;

        if (type === 'github' || type === 'github-enterprise') {
            const { scmId, apiUrl } = this.props.githubConfig;

            typedPicker = (
                <GithubCredentialsPicker
                    scmId={scmId}
                    apiUrl={apiUrl}
                />
            );
        } else if (type === 'bitbucket-cloud' || type === 'bitbucket-server') {
            const { id: scmId, apiUrl } = this.props.scmSource;

            typedPicker = (
                <BbCredentialsPicker
                    scmId={scmId}
                    apiUrl={apiUrl}
                />
            );
        }

        return (
            <div className="credentials-picker">
                { React.cloneElement(typedPicker, { onStatus, onComplete }) }
            </div>
        );
    }
}

CredentialsPicker.propTypes = {
    type: PropTypes.string,
    onStatus: PropTypes.func,
    onComplete: PropTypes.func,
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
