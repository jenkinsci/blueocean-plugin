import React, { Component, PropTypes } from 'react';
import { EmptyStateView, FileSize, Table } from '@jenkins-cd/design-language';
import { Icon } from '@jenkins-cd/react-material-icons';
import Markdown from 'react-remarkable';
import { observer } from 'mobx-react';
import mobxUtils from 'mobx-utils';
import { UrlConfig, logging } from '@jenkins-cd/blueocean-core-js';

const logger = logging.logger('io.jenkins.blueocean.dashboard.artifacts');
const { func, object, string } = PropTypes;
const ZipFileDownload = (props) => {
    const { zipFile, t } = props;
    if (!zipFile) {
        return null;
    }

    const title = t('rundetail.artifacts.button.downloadAll.title', { defaultValue: 'Download all artifact as zip' });
    const href = `${UrlConfig.getJenkinsRootURL()}${zipFile}`;

    return (<div className="downloadAllArtifactsButton">
        <a className="btn-secondary" target="_blank" title={title} href={href}>
            {t('rundetail.artifacts.button.downloadAll.text', { defaultValue: 'Download All' })}
        </a>
    </div>);
};

ZipFileDownload.propTypes = {
    zipFile: string,
    t: func,
};


const ArtifactListingLimited = (props) => {
    const { artifacts, t } = props;

    if (!artifacts || artifacts.length < 100) {
        return null;
    }

    return (<div className="artifactListingLimited">
        <EmptyStateView tightSpacing>
            <Markdown>
                {t('rundetail.artifacts.limit', { defaultValue: 'Only showing the first 100 artifacts' })}
            </Markdown>
        </EmptyStateView>
    </div>);
};


ArtifactListingLimited.propTypes = {
    artifacts: object,
    t: func,
};
/**
 * Displays a list of artifacts from the supplied build run property.
 */
@observer
export default class RunDetailsArtifacts extends Component {
    componentWillMount() {
        this._fetchArtifacts(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._fetchArtifacts(nextProps);
    }

    componentWillUnmount() {
        this.artifacts = null;
    }

    _fetchArtifacts(props) {
        const { result } = props;
        if (result && result.state === 'FINISHED') {
            this.artifacts = this.context.activityService.fetchArtifacts(result._links.self.href);
        }
    }

    render() {
        const { result, t } = this.props;

        if (!result || !this.artifacts) {
            return null;
        }
        switch (this.artifacts.state) {
        case mobxUtils.PENDING: return null; // <div>Loading</div>;
        case mobxUtils.REJECTED: return null; // <div>Not found</div>;
        default:
        }
        const { artifactsZipFile: zipFile } = result;
        const artifacts = this.artifacts.value;

        if (!artifacts || !artifacts.length) {
            return (<EmptyStateView tightSpacing>
                <Markdown>
                    {t('EmptyState.artifacts', { defaultValue: 'There are no artifacts for this pipeline run.\n\n' })}
                </Markdown>
            </EmptyStateView>);
        }

        const headers = [
            { label: t('rundetail.artifacts.header.name', { defaultValue: 'Name' }), className: 'name' },
            { label: t('rundetail.artifacts.header.size', { defaultValue: 'Size' }), className: 'size' },
            { label: '', className: 'actions' },
        ];

        const style = { fill: '#4a4a4a' };

        const artifactsRendered = artifacts.map(artifact => {
            const urlArray = artifact.url.split('/');
            const fileName = urlArray[urlArray.length - 1];
            logger.debug('artifact - url:', artifact.url, 'artifact - fileName:', fileName);
            return (
                <tr key={artifact.url}>
                    <td>{artifact.path}</td>
                    <td>
                        <FileSize bytes={artifact.size} />
                    </td>
                    <td className="download">
                        <a target="_blank" download={fileName} title={t('rundetail.artifacts.button.download', { defaultValue: 'Download the artifact' })} href={`${UrlConfig.getJenkinsRootURL()}${artifact.url}`}>
                            <Icon style={style} icon="file_download" />
                        </a>
                    </td>
                </tr>
            );
        });

        return (
            <div>
                <ArtifactListingLimited artifacts={artifacts} t={t} />
                <Table headers={headers} className="artifacts-table">
                    { artifactsRendered }
                    <td colSpan="3"></td>
                </Table>
               <ZipFileDownload zipFile={zipFile} t={t} />
            </div>
        );
    }
}


RunDetailsArtifacts.contextTypes = {
    activityService: object.isRequired,
};

RunDetailsArtifacts.propTypes = {
    result: object,
    t: func,
};
