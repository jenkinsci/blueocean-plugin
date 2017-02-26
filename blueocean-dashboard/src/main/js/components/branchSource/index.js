import { PagerService } from '@jenkins-cd/blueocean-core-js';
import { BranchService } from './services/BranchService';
const pagerService = new PagerService();
const branchService = new BranchService(pagerService);
export { branchService as BranchService };
export { Branch } from './components/Branch';
