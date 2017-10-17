import { PagerService } from '@jenkins-cd/blueocean-core-js';
const pagerService = new PagerService();

export { GeneralLogPager } from './GeneralLogPager';

export { PipelinePager } from './PipelinePager';

export { LogPager } from './LogPager';

import { PagerProvider } from './PagerProvider';
export const pagerProvider = new PagerProvider(pagerService);
