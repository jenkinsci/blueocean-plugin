/**
 * Created by cmeyers on 8/18/16.
 */
import { observable, computed } from 'mobx';

/**
 * Holds one or more toasts in state for display in UI.
 */
export class ToastService {

    @observable toasts = [];

    newToast(toast) {
        if (!toast.id) {
            toast.id = Math.random() * Math.pow(10, 16);
        }

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

    @computed
    get count() {
        return this.toasts ? this.toasts.length : 0;
    }

}
