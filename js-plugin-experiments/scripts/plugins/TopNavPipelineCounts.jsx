import React, {Component} from 'react';

import {store} from '../blue-ocean';
// TODO: ^^^^^ get store from <Provider> or through jenkins, not import! See: https://goo.gl/jCbg08

export default class TopNavPipelineCounts extends Component {

    constructor() {
        super();
        this.state = {};
    }

    componentDidMount() {
        const update = () => {this.setState({pipelines:store.getState().pipelines.pipelines});};
        this.unsubscribe = store.subscribe(update);
        update();
    }

    componentWillUnmount() {
        this.unsubscribe();
    }

    render() {
        const pipelines = this.state.pipelines || [];
        return <div>
            Num Pipelines: {pipelines.length}
        </div>;
    }
}
