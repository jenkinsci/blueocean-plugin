import { PagerService } from '@jenkins-cd/blueocean-core-js';
const pagerService = new PagerService();
import { KaraokeApi } from './rest/KaraokeApi';
const karaokeApi = new KaraokeApi();
export { karaokeApi as KaraokeApi };

import { KaraokeService } from './services/KaraokeService';
const karaokeService = new KaraokeService(pagerService);
export { karaokeService as KaraokeService };

