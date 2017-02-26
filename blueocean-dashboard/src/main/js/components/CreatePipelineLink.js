import React from 'react';
import { Link } from 'react-router';

import { i18nTranslator, Security } from '@jenkins-cd/blueocean-core-js';

const t = i18nTranslator('blueocean-dashboard');

export default function CreatePipelineLink() {
    // show no creation link if insufficient permissions
    if (Security.isSecurityEnabled() && Security.isAnonymousUser()) {
        return null;
    }

    return (
        <Link to="/create-pipeline" className="btn-new-pipeline btn-link inverse">
            {t('home.header.button.createpipeline')}
        </Link>
    );
}
