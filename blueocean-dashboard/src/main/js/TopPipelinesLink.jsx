import React, { Component } from 'react';
import { Link } from 'react-router';
import { observer } from 'mobx-react';

import pipelinesActive from './PipelinesActive';


@observer
export default class TopPipelinesLink extends Component {
    render() {
        const className = pipelinesActive.isActive ? 'selected' : '';
        return (
            <Link className={className} to="/pipelines">Pipelines</Link>
        );
    }
}
