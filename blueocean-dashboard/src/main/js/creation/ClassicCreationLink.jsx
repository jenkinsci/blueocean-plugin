import React from 'react';

<<<<<<< HEAD
import { i18nTranslator, buildClassicCreateJobUrl } from '@jenkins-cd/blueocean-core-js';
import { Icon } from '@jenkins-cd/react-material-icons';
=======
import { i18nTranslator, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import { Icon } from '@jenkins-cd/design-language';
>>>>>>> upstream/master

const t = i18nTranslator('blueocean-dashboard');

export function ClassicCreationLink() {
    const newJobUrl = buildClassicCreateJobUrl();

    return (
        <a target="_blank" className={'classic-link'} href={newJobUrl}>
            <Icon icon="ActionExitToApp" size={24} style={{ marginRight: '8px' }} />

            <span>{t('creation.core.header.classic')}</span>
        </a>
    );
}
