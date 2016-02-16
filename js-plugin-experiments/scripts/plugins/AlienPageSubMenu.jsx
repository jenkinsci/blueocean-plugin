import React, {Component} from 'react';
import {extensionPointStore, ExtensionPoint} from '../blue-ocean';

export default class AlienPageSubMenu extends Component {
    render() {
        return <div>
        <ExtensionPoint name="jenkins.pipeline.alienPageSubMenu" />
        </div>
    }
}
