import { PropTypes, Component } from 'react';

export class ActionLink {
    constructor(props) {
        if (props) {
            for (const key of Object.keys(props)) {
                this[key] = props[key];
            }
        }
    }
    static propTypes = {
        name: PropTypes.string.isRequired,
        title: PropTypes.string.isRequired,
        component: PropTypes.instanceOf(Component),
        badgeText: PropTypes.string
    };
}
