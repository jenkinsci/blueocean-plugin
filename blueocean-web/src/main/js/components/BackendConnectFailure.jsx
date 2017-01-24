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
            // Force loading of fonts etc. We need this because we need to make
            // sure all required resources are available when there's no connection.
            const style = { display: 'none' }; // react balks if inline style not defined this way ... yawn !!!
            return (
                <Fullscreen className="hidden">
                    <div className="toast" style={style}>
                        <div className="text">ok</div>
                    </div>
                </Fullscreen>
            );
        }

        let message;
        let activity;
        let cssClass;
        if (!this.state.doPageReload) {
            cssClass = 'connection-lost';

            message = translate('Connection.lost.message', {
                defaultValue: 'Connection lost'
            });
            activity = translate('Connection.lost.activity', {
                defaultValue: 'waiting'
            });
        } else {
            cssClass = 'connection-ok';

            message = translate('Connection.ok.message', {
                defaultValue: 'Connection ok'
            });
            activity = translate('Connection.ok.activity', {
                defaultValue: 'reloading'
            });
            setTimeout(() => window.location.reload(true), 2500);
        }

        return (
            <Fullscreen className={`blockscreen ${cssClass}`}>
                <div className="toast">
                    <div className="text">{message}:<span className="activity">{activity}</span></div>
                </div>
            </Fullscreen>
        );
    }
}
