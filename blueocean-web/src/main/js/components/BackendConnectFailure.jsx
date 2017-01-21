/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import React, { Component } from 'react';
import { sseConnection, Fullscreen, i18nTranslator } from '@jenkins-cd/blueocean-core-js';

const translate = i18nTranslator('blueocean-web');

/**
 * Backend connection failure component.
 */
export class BackendConnectFailure extends Component {

    componentWillMount() {
        this.state = {
            connectionOkay: true,
            doPageReload: false,
        };

        // Connection error handling...
        const thisComponent = this;
        sseConnection.onError((e) => {
            // Check the connection...
            sseConnection.waitConnectionOk((connectionStatus) => {
                if (connectionStatus.connectError) {
                    // Connection "broken" ...
                    thisComponent.setState({
                        connectionOkay: false,
                        doPageReload: false,
                    });
                } else if (connectionStatus.connectErrorCount > 0) {
                    // Connection was "broken" (see above), but is
                    // ok again now ...
                    thisComponent.setState({
                        connectionOkay: false,
                        doPageReload: true,
                    });
                }
            });
        });
    }

    render() {
        if (this.state.connectionOkay) {
            return null;
        }

        let title;
        let message;
        let nextAction;
        if (!this.state.doPageReload) {
            title = translate('Connection.lost.heading', {
                defaultValue: 'Connection lost',
            });
            message = translate('Connection.lost.message', {
                defaultValue: 'The connection to the Jenkins Server has been lost.'
            });
            nextAction = translate('Connection.lost.nextaction', {
                defaultValue: 'Waiting to reconnect ...'
            });
        } else {
            title = translate('Connection.ok.heading', {
                defaultValue: 'Connection ok again',
            });
            message = translate('Connection.ok.message', {
                defaultValue: 'The connection to the Jenkins Server is okay again.'
            });
            nextAction = translate('Connection.ok.nextaction', {
                defaultValue: 'Reloading page now ...'
            });
            setTimeout(() => window.location.reload(true), 4000);
        }

        return (
            <Fullscreen className="not-found">
                <div className="message-box">
                    <h3>{title}</h3>
                    <div className="message">{message}</div>
                    <div className="message">{nextAction}</div>
                </div>
            </Fullscreen>
        );
    }
}
