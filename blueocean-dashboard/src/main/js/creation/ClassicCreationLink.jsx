import React from 'react';

import { i18nTranslator, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import { Icon } from '@jenkins-cd/design-language';

const t = i18nTranslator('blueocean-dashboard');

export function ClassicCreationLink() {
    const baseUrl = UrlConfig.getJenkinsRootURL();
    const newJobUrl = `${baseUrl}/view/All/newJob`;

    return (
        <a target="_blank" className={'classic-link'} href={newJobUrl}>
            <Icon icon="ActionExitToApp" size={24} style={{ marginRight: '8px' }} />

            <span>{t('creation.core.header.classic')}</span>
        </a>
    );
}
