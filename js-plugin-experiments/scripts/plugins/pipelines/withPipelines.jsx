import React, {Component} from 'react';

import {store} from '../../blue-ocean';
// TODO: ^^^^^ get store from jenkins global somehow not import

export default function withPipelines(nestedComponent) {
    // TODO: We need to abstract this again, so going forward we don't need a new wrapper for every store prop we're interested in.
    return class extends Component {

        componentWillMount() {
            const update = () => {this.setState({pipelines:store.getState().pipelines.pipelines});};
            this.unsubscribe = store.subscribe(update);
            update();
        }

        componentWillUnmount() {
            this.unsubscribe();
        }

        render() {
            const props = {
                ...this.props,
                dispatch: store.dispatch,
                pipelines:this.state.pipelines
            };
            return React.createElement(nestedComponent, props);
        }
    }
}