import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { WeatherIcon } from '@jenkins-cd/design-language';
import Extensions from '@jenkins-cd/js-extensions';
import { buildPipelineUrl } from '../util/UrlUtils';

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
        const { pipeline, showOrganization } = this.props;

        // Early out
        if (!pipeline) {
            return null;
        }
        const simple = !pipeline.branchNames;
        const {
            name,
            fullName,
            organization,
            weatherScore,
            numberOfSuccessfulBranches,
            numberOfFailingBranches,
            numberOfSuccessfulPullRequests,
            numberOfFailingPullRequests,
            } = pipeline;

        const hasPullRequests = !simple && (
            numberOfSuccessfulPullRequests || numberOfFailingPullRequests);

        const baseUrl = buildPipelineUrl(organization, fullName);
        const multiBranchURL = `${baseUrl}/branches`;
        const pullRequestsURL = `${baseUrl}/pr`;
        const activitiesURL = `${baseUrl}/activity`;

        const formattedName = fullName ? fullName.split('/').join(' / ') : '';
        const nameLink = (
            <Link to={activitiesURL}>
                { showOrganization ?
                    `${organization} / ${formattedName}` :
                    formattedName
                }
            </Link>
        );

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
            <tr data-name={name} data-organization={organization}>
                <td>{nameLink}</td>
                <td><WeatherIcon score={weatherScore} /></td>
                {
                    // fixme refactor the next 2 lines and the prior logic
                    // to create a react component out of it
                }
                <td>{multiBranchLink}</td>
                <td>{pullRequestsLink}</td>
                <td>
                    <Extensions.Renderer
                      extensionPoint="jenkins.pipeline.list.action"
                      store={this.context.store}
                      pipeline={this.props.pipeline}
                    />
                </td>
            </tr>
        );
    }
}

PipelineRowItem.propTypes = {
    pipeline: PropTypes.object.isRequired,
    showOrganization: PropTypes.bool,
};

PipelineRowItem.contextTypes = {
    location: PropTypes.object,
    store: PropTypes.object,
};
