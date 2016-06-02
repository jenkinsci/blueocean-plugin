import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { WeatherIcon } from '@jenkins-cd/design-language';
import { Favorite } from '@jenkins-cd/design-language';

import { urlPrefix } from '../config';

export default class PipelineRowItem extends Component {

    calculateResponse(passing, failing) {
        let response = '-';
        if (failing > 0) {
            response = (`${failing} failing`);
        } else if (passing > 0) {
            response = (`${passing} passing`);
        }
        return response;
    }

    render() {
        const { pipeline } = this.props;
        // Early out
        if (!pipeline) {
            return null;
        }
        const simple = !pipeline.branchNames;
        const {
            name,
            weatherScore,
            numberOfSuccessfulBranches,
            numberOfFailingBranches,
            numberOfSuccessfulPullRequests,
            numberOfFailingPullRequests,
            } = pipeline;

        const hasPullRequests = !simple && (
            numberOfSuccessfulPullRequests || numberOfFailingPullRequests);

        const multiBranchURL = `${urlPrefix}/${name}/branches`;
        const pullRequestsURL = `${urlPrefix}/${name}/pr`;
        const activitiesURL = `${urlPrefix}/${name}/activity`;
        const nameLink = <Link to={activitiesURL}>{name}</Link>;

        let multiBranchLabel = ' - ';
        let multiPrLabel = ' - ';
        let multiBranchLink = null;
        let pullRequestsLink = null;

        if (!simple) {
            multiBranchLabel = this.calculateResponse(
                numberOfSuccessfulBranches, numberOfFailingBranches);
            multiPrLabel = this.calculateResponse(
                numberOfSuccessfulPullRequests, numberOfFailingPullRequests);

            multiBranchLink = <Link to={multiBranchURL}>{multiBranchLabel}</Link>;

            if (hasPullRequests) {
                pullRequestsLink = <Link to={pullRequestsURL}>{multiPrLabel}</Link>;
            }
        } else {
            multiBranchLink = multiBranchLabel;
            pullRequestsLink = multiPrLabel;
        }

        // FIXME: Visual alignment of the last column
        return (
            <tr>
                <td>{nameLink}</td>
                <td><WeatherIcon score={weatherScore} /></td>
                {
                    // fixme refactor the next 2 lines and the prior logic
                    // to create a react component out of it
                }
                <td>{multiBranchLink}</td>
                <td>{pullRequestsLink}</td>
                <td><Favorite /></td>
            </tr>
        );
    }
}

PipelineRowItem.propTypes = {
    pipeline: PropTypes.object.isRequired,
};
