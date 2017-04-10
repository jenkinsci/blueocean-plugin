import React from 'react';
import { Link } from 'react-router';

import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
import securityUtils from '../util/security-utils';

const t = i18nTranslator('blueocean-dashboard');

export default function CreatePipelineLink() {
    if (!securityUtils.isCreationEnabled()) {
        return null;
    }

    return (
        <Link to="/create-pipeline" className="btn-new-pipeline btn-secondary inverse">
            {t('home.header.button.createpipeline')}
        </Link>
    );
}
