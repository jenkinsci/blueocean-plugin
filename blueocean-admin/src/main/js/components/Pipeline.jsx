import React, { Component, PropTypes } from 'react';
import { components } from '@jenkins-cd/design-language';
const { WeatherIcon } = components;

export default class Pipeline extends Component {

    constructor(props) {
        super(props);
        this.state = { clicked: false };
    }

    calculateResponse (passing, failing) {
        if (failing > 0) {
            return (`${failing} failing`);
        } else if (passing > 0) {
            return (`${passing} passing`);
        } else {
            return '-';
        }
    }

    render() {
        const { pipeline, simple = false, hack } = this.props;

        const {
            name,
            weatherScore,
            numberOfSuccessfulBranches,
            numberOfFailingBranches,
            numberOfSuccessfulPullRequests,
            numberOfFailingPullRequests,
        } = pipeline;

        let multiBranch;
        let multiPr;

        if (!simple) {
            multiBranch = (<td>
                {this.calculateResponse(numberOfSuccessfulBranches, numberOfFailingBranches)}
            </td>);
            multiPr = (<td>
                {this.calculateResponse(numberOfSuccessfulPullRequests, numberOfFailingPullRequests)}
            </td>);
        } else {
            multiBranch = multiPr = (<td> - </td>);
        }

        return (<tr key={name}>
      <td>
        {name}
      </td>
      <td><WeatherIcon score={weatherScore} /></td>
      {multiBranch}
      {multiPr}
      <td>
        <i className="material-icons">&#xE83A;</i>
        { !simple && <button
          onClick={hack.MultiBranch.bind(null, pipeline)}
        >multiBranch</button>}
        { !simple &&
          (numberOfSuccessfulPullRequests > 0 || numberOfFailingPullRequests > 0)
          && <button
          onClick={hack.Pr.bind(null, pipeline)}
        >pr</button>}
        <button
          onClick={hack.Activity.bind(null, pipeline)}
        >Activities</button>
      </td>
    </tr>);
    }
}

Pipeline.propTypes = {
    pipeline: PropTypes.object.isRequired,
    simple: PropTypes.bool,
    hack: PropTypes.object.isRequired,
};
