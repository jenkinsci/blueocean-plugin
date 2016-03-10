import React, {Component, PropTypes} from 'react';
import Pipelines from './Pipelines';
import MultiBranch from './MultiBranch';

export default class Dashboard extends Component {

    constructor(props) {
        super(props);
        this.state = {view: 'pipelines'};
    }

    showPipelinesView() {
        this.setState({
            pipeline: null,
            view: 'pipelines'
        });
    }

    showMultiBranchView(pipeline) {
        this.setState({
            pipeline: pipeline,
            view: 'multiBranch'
        });
    }

    render() {
        const
            { pipelines } = this.props,
            { pipeline } = this.state,
            link = <a target='_blank' href="/jenkins/view/All/newJob">New Pipeline</a>;

        if (this.state.view === 'pipelines' && pipelines && pipelines.size > 0) {
            return <Pipelines link={link}
                              pipelines={pipelines}
                              hack={(pipeline) => { this.showMultiBranchView(pipeline) }}/>;
        }

        if (this.state.view === 'multiBranch') {
            return <MultiBranch pipeline={pipeline} back={ () => this.showPipelinesView() }/>;
        }
    }
}

Dashboard.propTypes = {
    pipelines: PropTypes.object.isRequired
};
