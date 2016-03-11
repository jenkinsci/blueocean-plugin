import React, { Component, PropTypes } from 'react';
import Pipelines from './Pipelines';
import Activity from './Activity';
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
    showActivityView(pipeline) {
        this.setState({
            pipeline: pipeline,
            view: 'ACTIVITY'
        });
    }

    render() {
        const { pipelines } = this.props;
        const { pipeline } = this.state;
        const link = <a target="_blank" href="/jenkins/view/All/newJob">New Pipeline</a>;

        const hack = {
            MultiBranch: (pipeline) => { this.showMultiBranchView(pipeline) },
            Activity: (pipeline) => { this.showActivityView(pipeline) },
        };
        if (this.state.view === 'pipelines' && pipelines && pipelines.size > 0) {
            return (<Pipelines link={link}
              pipelines={pipelines}
              hack={hack} />);
        }

        if (this.state.view === 'multiBranch') {
            // eslint-disable-next-line react/jsx-no-bind
            return (<MultiBranch pipeline={pipeline} back={ () => this.showPipelinesView() } />);
        }

        if (this.state.view === 'activity') {
            return <Activity pipeline={pipeline} back={ () => this.showPipelinesView() }/>;
        }
        return null;
    }
}

Dashboard.propTypes = {
    pipelines: PropTypes.object.isRequired,
};
