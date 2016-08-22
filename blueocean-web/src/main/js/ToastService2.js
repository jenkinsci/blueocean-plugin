/**
 * Created by cmeyers on 8/22/16.
 */
import { observable } from 'mobx';

export class ToastService2 {
    @observable toasts = [];

    newToast(toast) {
        // TODO: determine why it's necessary to re-set the "toasts" field to trigger the UI update
        const copy = this.toasts.slice();
        copy.push(toast);
        this.toasts = copy;
    }

    removeToast(toast) {
        this.toasts = this.toasts.filter((item) => {
            return toast.id !== item.id;
        });
    }
}
