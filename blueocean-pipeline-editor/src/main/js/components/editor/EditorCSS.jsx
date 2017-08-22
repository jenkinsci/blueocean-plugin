// @flow

import React, { Component } from 'react';

// Noddy component to render via ExtensionRenderer
// and trigger CSS loading from this plugin
export class EditorCSS extends Component {
    render() {
        return (<span />);
    }
}

export default EditorCSS;

// TODO: Make the route extensionpoint do the same loading/unloading as the component extensionpoint, then remove this
