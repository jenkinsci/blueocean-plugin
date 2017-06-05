import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { ExpandablePath, WeatherIcon, TableRow, TableCell } from '@jenkins-cd/design-language';
import Extensions from '@jenkins-cd/js-extensions';
import { buildPipelineUrl } from '../util/UrlUtils';
import { capable, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import { MATRIX_PIPELINE } from '../Capabilities';
import { Icon } from '@jenkins-cd/react-material-icons';

// Generate classic URL to redirect matrix-style / multiconfig jobs.
function generateRedirectURL(pipeline) {
    if (capable(pipeline, MATRIX_PIPELINE)) {
        return `${UrlConfig.getJenkinsRootURL()}${pipeline._links.self.href}`;
    }
    return null;
}

// Intercept click events so they don't bubble back to containing components
function cancelClick(e) {
    // TODO: Find other things doing the same and merge this
    e.stopPropagation();
    e.preventDefault();
}

export class PipelineRowItem extends Component {

    calculateResponse(passing, failing) {
        const { t } = this.props;
        let response = ' - ';
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
        const { pipeline, showOrganization, columns } = this.props;

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
            }  else {
                pullRequestsLink = multiPrLabel;
            }
        } else {
            multiBranchLink = multiBranchLabel;
            pullRequestsLink = multiPrLabel;
        }

        // Build the row link properties. Matrix jobs get sent to classic, hence the logic here.
        const linkProps = {};
        const matrixRedirectURL = generateRedirectURL(pipeline);

        if (matrixRedirectURL) {
            // Use a regular anchor, and target to a new tab
            linkProps.href = matrixRedirectURL;
            linkProps.className = 'pipelineRedirectLink';
            linkProps.target = '_blank';
        } else {
            // This is a normal pipeline job, so we use <Link> as usual
            linkProps.linkTo = activitiesURL;
            linkProps.query = location.query;
        }

        return (
            <TableRow data-pipeline={name} data-organization={organization} columns={columns} {...linkProps}>
                <TableCell className="TableCell--pipelineLink">
                    <ExpandablePath path={fullDisplayPath} />
                    { matrixRedirectURL && <Icon size={24} icon="exit_to_app" /> }
                </TableCell>
                <TableCell><WeatherIcon score={weatherScore} /></TableCell>
                <TableCell>{multiBranchLink}</TableCell>
                <TableCell>{pullRequestsLink}</TableCell>
                <TableCell className="TableCell--actions" onClick={cancelClick}>
                    <Extensions.Renderer
                      extensionPoint="jenkins.pipeline.list.action"
                      store={this.context.store}
                      pipeline={this.props.pipeline}
                    />
                </TableCell>
            </TableRow>
        );
    }
}

PipelineRowItem.propTypes = {
    pipeline: PropTypes.object.isRequired,
    showOrganization: PropTypes.bool,
    t: PropTypes.func,
    columns: PropTypes.object,
};

PipelineRowItem.contextTypes = {
    location: PropTypes.object,
    store: PropTypes.object,
};

export default PipelineRowItem;
