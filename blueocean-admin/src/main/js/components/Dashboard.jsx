import React, { Component, PropTypes } from 'react';
import Pipelines from './Pipelines';
import Activity from './Activity';
import MultiBranch from './MultiBranch';
import Prs from './Prs';

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
            pipeline,
            view: 'multiBranch',
        });
    }

    showPrView(pipeline) {
        this.setState({
            pipeline,
            view: 'pr',
        });
    }

    showActivityView(pipeline) {
        this.setState({
            pipeline,
            view: 'activity',
        });
    }

    render() {
        const { pipelines } = this.props;
        const { pipeline } = this.state;
        const link = <a target="_blank" className="btn-primary" href="/jenkins/view/All/newJob">New Pipeline</a>;
        const back = () => {
            this.showPipelinesView();
        };

        /*
         this is a hack that will be removed when we have routes in place and we get
         the pipeline from there. Since this is not the original design and to make
         sure it gets removed and raises questions it is called hack, since ux-81.
         */
        const hack = {
            MultiBranch: (pipe) => { this.showMultiBranchView(pipe); },
            Pr: (pipe) => { this.showPrView(pipe); },
            Activity: (pipe) => { this.showActivityView(pipe); },
        };
        if (this.state.view === 'pipelines' && pipelines && pipelines.size > 0) {
            return (<Pipelines link={link}
              pipelines={pipelines}
              hack={hack}
            />);
        }

        if (this.state.view === 'multiBranch') {
            // eslint-disable-next-line react/jsx-no-bind
            return (<MultiBranch pipeline={pipeline} back={back} />);
        }

        if (this.state.view === 'activity') {
            return <Activity pipeline={pipeline} back={back} />;
        }

        if (this.state.view === 'pr') {
            return <Prs pipeline={pipeline} back={back} />;
        }
        return null;
    }
}

Dashboard.propTypes = {
    pipelines: PropTypes.object.isRequired,
};
