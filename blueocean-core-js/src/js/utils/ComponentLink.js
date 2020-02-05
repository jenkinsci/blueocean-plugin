import { PropTypes, Component } from 'react';
import { SandboxedComponent } from '@jenkins-cd/js-extensions';

export class ComponentLink {
    constructor(props) {
        if (props) {
            for (const key of Object.keys(props)) {
                this[key] = props[key];
            }
        }
    }
    get view() {
        const children = this.component;
        return class ViewRenderer extends SandboxedComponent {
            constructor(props) {
                super();
                this.children = children;
            }
        };
    }
    static propTypes = {
        name: PropTypes.string.isRequired,
        title: PropTypes.string.isRequired,
        notification: PropTypes.string,
        component: PropTypes.element.isRequired,
    };
}
