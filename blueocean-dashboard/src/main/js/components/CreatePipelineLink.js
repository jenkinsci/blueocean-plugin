import React from 'react';
import { Link } from 'react-router';

import { i18nTranslator, AppConfig } from '@jenkins-cd/blueocean-core-js';
import creationUtils from '../creation/creation-status-utils';

const t = i18nTranslator('blueocean-dashboard');

export default function CreatePipelineLink() {
    if (creationUtils.isHidden()) {
        return null;
    }
    const organization = AppConfig.getOrganizationName();
    const link = organization ? `/organizations/${organization}/create-pipeline` : '/create-pipeline';

    return (
        <Link to={link} className="btn-new-pipeline btn-secondary inverse">
            {t('home.header.button.createpipeline')}
        </Link>
    );
}
