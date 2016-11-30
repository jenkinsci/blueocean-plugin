/**
 * Created by cmeyers on 10/20/16.
 */
import React, { PropTypes } from 'react';
import { Link } from 'react-router';
import { i18nTranslator, Security, UrlConfig } from '@jenkins-cd/blueocean-core-js';

const translate = i18nTranslator('blueocean-dashboard');

const QUERY_STRING_KEY = 'blueCreate';

export default function CreatePipelineLink(props, context) {
    const caption = translate('home.header.button.createpipeline', { defaultValue: 'New Pipeline' });
    // if special key is not defined, create a link to classic UI
    if (!context.location || !context.location.query || !(QUERY_STRING_KEY in context.location.query)) {
        const baseUrl = UrlConfig.getJenkinsRootURL();
        const newJobUrl = `${baseUrl}/view/All/newJob`;

        return (
            <a target="_blank" className="btn-secondary inverse" href={newJobUrl}>
                { caption }
            </a>
        );
    }

    // show no creation link if insufficient permissions
    if (Security.isSecurityEnabled() && Security.isAnonymousUser()) {
        return null;
    }

    return (
        <Link to="/create-pipeline" className="btn-secondary inverse">
            { caption }
        </Link>
    );
}

CreatePipelineLink.contextTypes = {
    location: PropTypes.object,
};
