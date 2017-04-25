/**
 * Created by cmeyers on 9/21/16.
 */

import { ToastService as toastService } from './index';
import { buildRunDetailsUrl } from './UrlBuilder';
import i18nTranslator from './i18n/i18n';

export default {
    /**
     *
     * @param runnable
     * @param run
     * @param toastAction
     */
    createRunStartedToast: (runnable, run, toastAction) => {
        const translate = i18nTranslator('blueocean-web');

        const runId = run.id;

        const runDetailsUrl = buildRunDetailsUrl(run);

        const name = decodeURIComponent(runnable.name);
        const text = translate('toast.run.started', {
            0: name,
            1: runId,
            defaultValue: 'Started "{0}" #{1}',
        });

        const caption = translate('toast.run.open', {
            defaultValue: 'Open',
        });
        toastService.newToast({
            text,
            action: caption,
            onActionClick: () => {
                if (toastAction) {
                    toastAction(runDetailsUrl);
                }
            },
        });

        return runDetailsUrl;
    },
};
