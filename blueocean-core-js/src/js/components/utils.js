/**
 * Created by cmeyers on 9/1/16.
 */
import { ToastService as toastService } from '../';
import { CAPABILITIES, capabilityStore } from '../';
import { buildRunDetailsUrlFromQueue } from '../UrlBuilder';

export const createRunStartedToast = (runnable, runInfo, toastAction) => {
    const className = runnable._class;

    capabilityStore.resolveCapabilities(className)
        .then(capabilityMap => {
            const capabilities = capabilityMap[className];
            const isMultiBranch = capabilities.some(capability => (
                [CAPABILITIES.MULTIBRANCH_BRANCH, CAPABILITIES.MULTIBRANCH_PIPELINE].indexOf(capability) !== -1
            ));

            const runId = runInfo.expectedBuildNumber;
            // TODO: href doesn't encode branch name correctly; verify bug fix after JENKINS-37873 is resolved
            const runDetailsUrl = buildRunDetailsUrlFromQueue(
                runInfo._links.self.href,
                isMultiBranch,
                runId,
                true
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
        });
};
