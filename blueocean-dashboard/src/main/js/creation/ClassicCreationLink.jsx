import React from 'react';

import { i18nTranslator, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import { Icon } from '@jenkins-cd/react-material-icons';

const t = i18nTranslator('blueocean-dashboard');

export function ClassicCreationLink() {
    const baseUrl = UrlConfig.getJenkinsRootURL();
    const newJobUrl = `${baseUrl}/view/All/newJob`;

    return (
        <a target="_blank" className={'classic-link'} href={newJobUrl}>
            <Icon icon="input" />

            <span>{t('creation.core.header.classic')}</span>
        </a>
    );
}
