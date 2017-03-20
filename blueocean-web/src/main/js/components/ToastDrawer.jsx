/**
 * Created by cmeyers on 8/23/16.
 */
import React, { Component, PropTypes } from 'react';
import { observer } from 'mobx-react';

import { Toaster } from '@jenkins-cd/design-language';
import { ToastService as toastService } from '@jenkins-cd/blueocean-core-js';

/**
 * Provides a UI for displaying toasts and binds it with the shared ToastService.
 * Ensures that toasts are removed after user interaction / dismiss timeout.
 */
@observer
export class ToastDrawer extends Component {

    _removeToast(toast) {
        toastService.removeToast(toast);
    }

    render() {
        // need to reference observable prop in render to trigger subscription
        toastService.count;

        return (
            <Toaster
                toasts={toastService.toasts}
                onActionClick={(toast1) => this._removeToast(toast1)}
                onDismiss={(toast2) => this._removeToast(toast2)}
                dismissDelay={5000}
            />
        );
    }
}
