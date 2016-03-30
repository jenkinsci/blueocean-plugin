import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { components } from '@jenkins-cd/design-language';
const { WeatherIcon } = components;

import { urlPrefix } from '../config';

export default class Pipeline extends Component {

    calculateResponse(passing, failing) {
        if (failing > 0) {
            return (`${failing} failing`);
        } else if (passing > 0) {
            return (`${passing} passing`);
        } else {
            return '-';
        }
    }

    render() {
        const { pipeline } = this.props;
        const simple = !pipeline.branchNames;
        const {
            name,
            weatherScore,
            numberOfSuccessfulBranches,
            numberOfFailingBranches,
            numberOfSuccessfulPullRequests,
            numberOfFailingPullRequests,
            } = pipeline;

        const hasPullRequests = !simple && (numberOfSuccessfulPullRequests || numberOfFailingPullRequests);

        const multiBranchURL = `${urlPrefix}/${name}/branches`;
        const pullRequestsURL = `${urlPrefix}/${name}/pr`;
        const activitiesURL = `${urlPrefix}/${name}/activity`;

        let multiBranchLabel = " - ";
        let multiPrLabel = " - ";
        let multiBranchLink = null;
        let pullRequestsLink = null;

        if (!simple) {
            multiBranchLabel = this.calculateResponse(numberOfSuccessfulBranches, numberOfFailingBranches);
            multiPrLabel = this.calculateResponse(numberOfSuccessfulPullRequests, numberOfFailingPullRequests);

            multiBranchLink = <Link className="btn" to={multiBranchURL}>multiBranch</Link>;

            if (hasPullRequests)
                pullRequestsLink = <Link className="btn" to={pullRequestsURL}>pr</Link>;
        }

        // FIXME: Visual alignment of the last column
        return (
            <tr>
                <td>{name}</td>
                <td><WeatherIcon score={weatherScore}/></td>
                <td>{multiBranchLabel}</td>
                <td>{multiPrLabel}</td>
                <td>
                    <i className="material-icons">&#xE83A;</i>
                    {multiBranchLink}
                    {pullRequestsLink}
                    <Link className="btn" to={activitiesURL}>Activities</Link>
                </td>
            </tr>
        );
    }
}

Pipeline.propTypes = {
    pipeline: PropTypes.object.isRequired
};
