/**
 * Created by cmeyers on 8/18/16.
 */
import { observable } from 'mobx';

export class ToastService {

    @observable toasts = [];

    constructor() {
        console.log('hello from ToastService');
    }

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
