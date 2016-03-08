import React, {Component, PropTypes} from 'react';
import Pipelines from './Pipelines';
import Immutable from 'immutable';
import {components} from 'jenkins-design-language';

const { Page } = components;

export default class Dashboard extends Component {
   constructor() {
    super();
    this.state = {view: 'pipelines'};
  }
  render() {
    const
      { pipelines } = this.props,
      link = <a target='_blank' href="/jenkins/view/All/newJob">New Pipeline</a>;

    return <Page>
      <div>CloudBees {link}</div>
      {(this.state.view ==='pipelines' && pipelines && pipelines.size > 0) ? <Pipelines pipelines={pipelines}/> : link}
    </Page>;
  }
}

Dashboard.propTypes = {
  pipelines: PropTypes.object.isRequired
};
