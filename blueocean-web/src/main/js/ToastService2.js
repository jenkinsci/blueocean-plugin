/**
 * Created by cmeyers on 8/22/16.
 */
import { observable } from 'mobx';
import Immutable from 'immutable';
const { List } = Immutable;

export class ToastService2 {
    @observable toasts = new List();

    newToast(toast) {
        this.toasts = this.toasts.push(toast);
    }
}
