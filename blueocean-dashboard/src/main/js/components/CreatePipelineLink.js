import React from 'react';
import { Link } from 'react-router';

import { i18nTranslator, User } from '@jenkins-cd/blueocean-core-js';

const t = i18nTranslator('blueocean-dashboard');

export default function CreatePipelineLink() {
    const user = User.current();

    if (user && !user.permissions.pipeline.create()) {
        return null;
    }

    return (
        <Link to="/create-pipeline" className="btn-new-pipeline btn-link inverse">
            {t('home.header.button.createpipeline')}
        </Link>
    );
}
