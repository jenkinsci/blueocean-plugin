import { preferences } from '@jenkins-cd/preferences';
import { KaraokeApi } from './rest/KaraokeApi';
const followingApi = new KaraokeApi();
export { followingApi as KaraokeApi };
export const KaraokeSpeed = 1000;

/**
 * Preferences that we support in following and detail view
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
        key: 'runDetails.pipeline.following',
        defaultValue: 'default',
        allowedValues: ['default', 'never'],
    },
    {
        key: 'runDetails.pipeline.stopKaraokeOnAnyNodeClick',
        defaultValue: 'default',
        allowedValues: ['default', 'always'],
    },
];

const followingConfig = preferences.newPreferences(preferencesArray);

export { followingConfig as KaraokeConfig };
