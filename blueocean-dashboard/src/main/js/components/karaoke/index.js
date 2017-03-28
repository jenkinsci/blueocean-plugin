import { PagerService } from '@jenkins-cd/blueocean-core-js';
import { preferences } from '@jenkins-cd/preferences';
const pagerService = new PagerService();
import { KaraokeApi } from './rest/KaraokeApi';
const karaokeApi = new KaraokeApi();
export { karaokeApi as KaraokeApi };

import { KaraokePagerService } from './services/KaraokeService';
const karaokeService = new KaraokePagerService(pagerService);
export { karaokeService as KaraokeService };
/**
 * Preferences that we support in karaoke and detail view
 * @type {[{key: string,
 *       defaultValue: string,
 *       allowedValues: ['classic', 'pipeline'],
 *   }]}
 */
export const preferencesArray = [{
    key: 'runDetails.logView',
    defaultValue: 'pipeline',
    allowedValues: ['classic', 'pipeline'],
},
    {
        key: 'runDetails.pipeline.updateOnFinish',
        defaultValue: 'default',
        allowedValues: ['default', 'never'],
    },
    {
        key: 'runDetails.pipeline.showPending',
        defaultValue: 'never',
        allowedValues: ['always', 'never'],
    },
    {
        key: 'runDetails.pipeline.karaoke',
        defaultValue: 'default',
        allowedValues: ['default', 'never'],
    },
    {
        key: 'runDetails.pipeline.stopKaraokeOnAnyNodeClick',
        defaultValue: 'default',
        allowedValues: ['default', 'always'],
    },
];

const karaokeConfig = preferences.newPreferences(preferencesArray);

export { karaokeConfig as KaraokeConfig };
