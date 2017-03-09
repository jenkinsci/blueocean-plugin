import { PagerService } from '@jenkins-cd/blueocean-core-js';
const pagerService = new PagerService();
import { KaraokeApi } from './rest/KaraokeApi';
const karaokeApi = new KaraokeApi();
export { karaokeApi as KaraokeApi };

import { KaraokePagerService } from './services/KaraokeService';
const karaokeService = new KaraokePagerService(pagerService);
export { karaokeService as KaraokeService };

