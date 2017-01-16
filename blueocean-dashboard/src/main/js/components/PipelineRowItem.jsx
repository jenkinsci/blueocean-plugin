import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { ExpandablePath, WeatherIcon } from '@jenkins-cd/design-language';
import Extensions from '@jenkins-cd/js-extensions';
import { buildPipelineUrl } from '../util/UrlUtils';
import { capable, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import { MATRIX_PIPELINE } from '../Capabilities';
import { Icon } from '@jenkins-cd/react-material-icons';

function generateRedirectLink(pipeline) {
    if (capable(pipeline, MATRIX_PIPELINE)) {
        return (<a
          className="pipelineRedirectLink"
          href={`${UrlConfig.getJenkinsRootURL()}${pipeline._links.self.href}`}
          target="_blank"
        >
            {pipeline.fullDisplayName}<Icon size={24} icon="exit_to_app" />
        </a>);
    }

    return null;
}
export class PipelineRowItem extends Component {

    calculateResponse(passing, failing) {
        const { t } = this.props;
        let response = '-';
        if (failing > 0) {
            response = t('home.pipelineslist.row.failing', {
                0: failing,
                defaultValue: '{0} failing',
            });
        } else if (passing > 0) {
            response = t('home.pipelineslist.row.passing', {
                0: passing,
                defaultValue: '{0} passing',
            });
        }
        return response;
    }

    render() {
        const { pipeline, showOrganization } = this.props;

        // Early out
        if (!pipeline) {
            return null;
        }
        const { location = {} } = this.context;
        const simple = !pipeline.branchNames;
        const {
            name,
            fullName,
            fullDisplayName,
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

        const fullDisplayPath = showOrganization ? `${organization}/${fullDisplayName}` : fullDisplayName;
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
                <td>
                    {
                        generateRedirectLink(pipeline) ||
                        <Link to={activitiesURL} query={location.query}>
                            <ExpandablePath path={fullDisplayPath} />
                        </Link>
                    }
                </td>
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

export default PipelineRowItem;
