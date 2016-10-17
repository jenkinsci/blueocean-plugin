import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { WeatherIcon } from '@jenkins-cd/design-language';
import Extensions from '@jenkins-cd/js-extensions';
import { translate } from 'react-i18next';
import { buildPipelineUrl } from '../util/UrlUtils';

export class PipelineRowItem extends Component {

    calculateResponse(passing, failing) {
        const { t } = this.props;
        let response = '-';
        if (failing > 0) {
            response = t('bo.dashboard.failing', { 0: failing});
        } else if (passing > 0) {
            response = t('bo.dashboard.passing', { 0: passing});
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
            displayName,
            } = pipeline;

        const hasPullRequests = !simple && (
            numberOfSuccessfulPullRequests || numberOfFailingPullRequests);

        const baseUrl = buildPipelineUrl(organization, fullName);
        const multiBranchURL = `${baseUrl}/branches`;
        const pullRequestsURL = `${baseUrl}/pr`;
        const activitiesURL = `${baseUrl}/activity`;

        const pathInJob = fullName.split('/').slice(0, -1).join(' / ');
        const formattedName = `${pathInJob ? `${pathInJob} / ` : ''}${displayName}`;
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
    t: PropTypes.func,
};

PipelineRowItem.contextTypes = {
    location: PropTypes.object,
    store: PropTypes.object,
};

export default translate(['jenkins.plugins.blueocean.dashboard.Messages'], { wait: true })(PipelineRowItem);
