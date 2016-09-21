/**
 * Created by cmeyers on 9/21/16.
 */

import { ToastService as toastService } from './index';
import { buildRunDetailsUrlFromQueue } from './UrlBuilder';

const CAPABILITY_MULTIBRANCH_PIPELINE = 'io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline';
const CAPABILITY_MULTIBRANCH_BRANCH = 'io.jenkins.blueocean.rest.model.BlueBranch';


export default {

    /**
     *
     * @param runnable
     * @param runInfo
     * @param toastAction
     */
    createRunStartedToast: (runnable, runInfo, toastAction) => {
        const isMultiBranch = runnable._capabilities.some(capability => (
            [CAPABILITY_MULTIBRANCH_PIPELINE, CAPABILITY_MULTIBRANCH_BRANCH].indexOf(capability) !== -1
        ));

        const runId = runInfo.expectedBuildNumber;

        const runDetailsUrl = buildRunDetailsUrlFromQueue(
            runInfo._links.self.href,
            isMultiBranch,
            runId,
        );

        const name = decodeURIComponent(runnable.name);

        toastService.newToast({
            text: `Started "${name}" #${runId}`,
            action: 'Open',
            onActionClick: () => {
                if (toastAction) {
                    toastAction(runDetailsUrl);
                }
            },
        });
    },
};
