import React, {Component, PropTypes} from 'react';
import Pipelines from './Pipelines';
import MultiBranch from './MultiBranch';
import {components} from 'jenkins-design-language';

const { Page } = components;

export default class Dashboard extends Component {

   constructor(props) {
    super(props);
    this.state = {view: 'pipelines'};
  }

  render() {
    const
      { pipelines } = this.props,
      link = <a target='_blank' href="/jenkins/view/All/newJob">New Pipeline</a>;

    return <Page>
      {(this.state.view ==='pipelines' && pipelines && pipelines.size > 0)
        && <Pipelines
          link={link}
          pipelines={pipelines}
          hack={(pipeline) => {
            this.setState({
            pipeline: pipeline,
            view: 'multiBranch'
            });
          }}
        />}

      { this.state.view ==='multiBranch' && <MultiBranch
        pipeline={this.state.pipeline} back={ () =>  this.setState({
          pipeline: null,
          view: 'pipelines'
          })
        }/>}
    </Page>;
  }
}

Dashboard.propTypes = {
  pipelines: PropTypes.object.isRequired
};
