import React, { Component, PropTypes } from 'react';
import { FileSize, JTable, TableRow, TableCell, TableHeaderRow } from '@jenkins-cd/design-language';
import { Icon } from '@jenkins-cd/design-language';
import { observer } from 'mobx-react';
import mobxUtils from 'mobx-utils';
import { logging, UrlConfig } from '@jenkins-cd/blueocean-core-js';

const logger = logging.logger('io.jenkins.blueocean.dashboard.artifacts');
const t = require('@jenkins-cd/blueocean-core-js').i18nTranslator('blueocean-dashboard');

const ZipFileDownload = (props) => {
    const { zipFile } = props;
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
    zipFile: PropTypes.string,
};


function ArtifactListingLimited(props) {
    return (
        <div className="artifacts-info-container">
            <div className="artifacts-info">
                <h1 className="title">{t('rundetail.artifacts.limit_title')}</h1>
                <p className="message">{t('rundetail.artifacts.limit_message')}</p>
            </div>
        </div>
    );
}


/**
 * Displays a list of artifacts from the supplied build run property.
 */
@observer
export class RunDetailsArtifacts extends Component {
    componentWillMount() {
        this._fetchArtifacts(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._fetchArtifacts(nextProps);
    }

    componentWillUnmount() {
        this.artifactsPromise = null;
    }

    _fetchArtifacts(props) {
        const result = props.result;
        if (!result) {
            return;
        }
        this.artifactsPromise = this.context.activityService.fetchArtifacts(result._links.self.href);
    }

    render() {
        const { result } = this.props;

        const promise = this.artifactsPromise;
        if (!result || !promise || promise.state === mobxUtils.PENDING || promise.state === mobxUtils.REJECTED) {
            return null;
        }

        const { artifactsZipFile: zipFile } = result;
        const artifacts = promise.value;

        const nameLabel = t('rundetail.artifacts.header.name', { defaultValue: 'Name' });
        const sizeLabel = t('rundetail.artifacts.header.size', { defaultValue: 'Size' });
        const downloadLabel = t('rundetail.artifacts.button.download', { defaultValue: 'Download the artifact' });
        const openLabel = t('rundetail.artifacts.button.open', { defaultValue: 'Open the artifact' });

        const columns = [
            JTable.column(500, nameLabel, true),
            JTable.column(120, sizeLabel),
            JTable.column(50, ''),
        ];

        const rootURL = UrlConfig.getJenkinsRootURL();

        const artifactsRendered = artifacts.map(artifact => {
            const urlArray = artifact.url.split('/');
            const fileName = urlArray[urlArray.length - 1];
            logger.debug('artifact - url:', artifact.url, 'artifact - fileName:', fileName);

            let downloadLink = null;
            if (artifact.downloadable) {
                downloadLink = (
                    <a target="_blank"
                       className="action-button-colors"
                       download={fileName} title={downloadLabel}
                       href={`${rootURL}${artifact.url}`}
                    >
                        <Icon icon="FileFileDownload" color="rgba(53, 64, 82, 0.25)" />
                    </a>
                );
            }

            const artifactSize = artifact.size >= 0 ? (<FileSize bytes={artifact.size} />) : (<span>–</span>);

            return (
                <TableRow key={artifact.url}>
                    <TableCell>
                        <a target="_blank" title={openLabel} href={`${rootURL}${artifact.url}`}>
                            {artifact.path}
                        </a>
                    </TableCell>
                    <TableCell>
                        {artifactSize}
                    </TableCell>
                    <TableCell className="TableCell--actions">
                        {downloadLink}
                    </TableCell>
                </TableRow>
            );
        });

        const logOpenURL = `${rootURL}${result._links.self.href}log/?start=0`;
        const logDownloadURL = `${rootURL}${result._links.self.href}log/?start=0&download=true`;

        return (
            <div>
                { artifacts.length > 100 && <ArtifactListingLimited t={t} /> }
                <JTable columns={columns} className="artifacts-table">
                    <TableHeaderRow />
                    <TableRow>
                        <TableCell>
                            <a target="_blank" title={openLabel} href={logOpenURL}>
                                pipeline.log
                            </a>
                        </TableCell>
                        <TableCell>-</TableCell>
                        <TableCell className="TableCell--actions">
                            <a target="_blank" className="action-button-colors" title={downloadLabel} href={logDownloadURL}>
                                <Icon icon="FileFileDownload" color="rgba(53, 64, 82, 0.25)" />
                            </a>
                        </TableCell>
                    </TableRow>
                    { artifactsRendered }
                </JTable>
                <ZipFileDownload zipFile={zipFile} t={t} />
            </div>
        );
    }
}

RunDetailsArtifacts.contextTypes = {
    activityService: PropTypes.object.isRequired,
};

RunDetailsArtifacts.propTypes = {
    result: PropTypes.object,
};

export default {
    name: "artifacts",
    title: t('rundetail.header.tab.artifacts'),
    component: RunDetailsArtifacts,
};
