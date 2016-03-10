import React, { Component, PropTypes } from 'react';
import Pipelines from './Pipelines';
import MultiBranch from './MultiBranch';

export default class Dashboard extends Component {

    constructor(props) {
        super(props);
        this.state = { view: 'pipelines' };
    }

    showPipelinesView() {
        this.setState({
            pipeline: null,
            view: 'pipelines',
        });
    }

    showMultiBranchView(pipeline) {
        this.setState({
            pipeline: pipeline, // eslint-disable-line object-shorthand
            view: 'multiBranch',
        });
    }

    render() {
        const { pipelines } = this.props;
        const { pipeline } = this.state;
        const link = <a target="_blank" href="/jenkins/view/All/newJob">New Pipeline</a>;

        if (this.state.view === 'pipelines' && pipelines && pipelines.size > 0) {
            return (<Pipelines link={link}
              pipelines={pipelines}
              hack={(pipeline) => { this.showMultiBranchView(pipeline); }} />); // eslint-disable-line
        }

        if (this.state.view === 'multiBranch') {
            // eslint-disable-next-line react/jsx-no-bind
            return (<MultiBranch pipeline={pipeline} back={ () => this.showPipelinesView() } />);
        }

        // TODO: Verify that this is ok.
        // There was an ESLint error because of the missing return.
        return null;
    }
}

Dashboard.propTypes = {
    pipelines: PropTypes.object.isRequired,
};
