import React, { Component, PropTypes } from 'react';
import { AppConfig, Security, UrlConfig, Utils } from '../index';

/**
 * LoginButton to login/logout.
 */
export class LoginButton extends Component {
    render() {
        if (!Security.isSecurityEnabled()) {
            return null;
        }

        let action;
        let url;
        if (Security.isAnonymousUser()) {
            action = 'login';
            url = `${UrlConfig.getJenkinsRootURL()}/${AppConfig.getLoginUrl()}?from=${encodeURIComponent(Utils.windowOrGlobal().location.pathname)}`;
        } else {
            action = 'logout';
            url = `${UrlConfig.getJenkinsRootURL()}/logout`;
        }
        return (
            <div className={this.props.className}>
                <a href={url} className="btn-link">
                    {this.props.translate(action, { defaultValue: action })}
                </a>
            </div>
        );
    }
}

LoginButton.propTypes = {
    className: PropTypes.string,
    translate: PropTypes.func,
};
