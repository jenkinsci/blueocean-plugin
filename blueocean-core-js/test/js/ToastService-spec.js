/**
 * Created by cmeyers on 8/22/16.
 */
import { assert } from 'chai';

import { ToastService } from '../../src/js/ToastService';

describe('ToastService', () => {
    let toastService;

    beforeEach(() => {
        toastService = new ToastService();
    });

    it('adds a new toast and creates ID if necessary', () => {
        toastService.newToast({
            text: 'Hello World',
            action: 'Dismiss',
        });

        assert.equal(toastService.count, 1);

        const toast = toastService.toasts[0];
        assert.equal(toast.text, 'Hello World');
        assert.equal(toast.action, 'Dismiss');
        assert.isOk(toast.id);
    });

    it('adds a new toast and preserves ID if necessary', () => {
        toastService.newToast({
            id: 12345,
            text: 'Hello World',
            action: 'Dismiss',
        });

        assert.equal(toastService.count, 1);

        const toast = toastService.toasts[0];
        assert.equal(toast.text, 'Hello World');
        assert.equal(toast.action, 'Dismiss');
        assert.equal(toast.id, 12345);
    });

    it('removes a toast', () => {
        toastService.newToast({
            id: 12345,
            text: 'Hello World',
            action: 'Dismiss',
        });
        toastService.newToast({
            id: 54321,
            text: 'Hello You',
            action: 'Dismiss',
        });

        assert.equal(toastService.count, 2);

        toastService.removeToast({
            id: 12345,
        });

        assert.equal(toastService.count, 1);
    });
});
