import {
    AppConfig,
    capable,
    doubleUriEncode,
    logging,
    capabilityAugmenter,
} from '@jenkins-cd/blueocean-core-js';

import { MULTIBRANCH_PIPELINE } from '../../../Capabilities';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.detailUrl');

export const generateDetailUrl = (pipeline, branch, runId) => {
    const baseUrl =
        `${AppConfig.getRestRoot()}/organizations/${pipeline.organization}/` +
        `pipelines/${pipeline.fullName}`;
    const isMultiBranchPipeline = capable(pipeline, MULTIBRANCH_PIPELINE);
    let returnUrl;
    if (isMultiBranchPipeline) {
        returnUrl = `${baseUrl}/branches/${doubleUriEncode(branch)}/runs/${runId}`;
    } else {
        returnUrl = `${baseUrl}/runs/${runId}`;
    }
    logger.debug(returnUrl);
    return returnUrl;
};
