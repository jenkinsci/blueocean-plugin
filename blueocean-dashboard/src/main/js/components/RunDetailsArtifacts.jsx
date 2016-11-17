import React, { Component, PropTypes } from 'react';
import { EmptyStateView, FileSize, Table } from '@jenkins-cd/design-language';
import { Icon } from 'react-material-icons-blue';
import Markdown from 'react-remarkable';

/**
 * Displays a list of artifacts from the supplied build run property.
 */
export default class RunDetailsArtifacts extends Component {

    render() {
        const { result, t } = this.props;

        if (!result) {
            return null;
        }

        const { artifacts } = result;

        if (!artifacts || !artifacts.length) {
            return (<EmptyStateView tightSpacing>
                <Markdown>
                    {t('EmptyState.artifacts', { defaultValue: 'There are no artifacts for this pipeline run.\n\n' })}
                </Markdown>
            </EmptyStateView>);
        }

        const headers = [
            { label: t('rundetail.artifacts.header.name', { defaultValue: 'Name' }), className: 'name' },
            { label: t('rundetail.artifacts.header.size', { defaultValue: 'Header' }), className: 'size' },
            { label: '', className: 'actions' },
        ];

        const style = { fill: '#4a4a4a' };

        return (
            <Table headers={headers} className="artifacts-table fixed">
                { artifacts.map(artifact => (
                    <tr key={artifact.url}>
                        <td>{artifact.name}</td>
                        <td>
                            <FileSize bytes={artifact.size} />
                        </td>
                        <td className="download">
                            <a target="_blank" title={t('rundetail.artifacts.button.download', { defaultValue: 'Download the artifact' })} href={artifact.url}>
                                <Icon style={style} icon="file_download" />
                            </a>
                        </td>
                    </tr>
                ))}
            </Table>
        );
    }
}

const { func, object } = PropTypes;

RunDetailsArtifacts.propTypes = {
    result: object,
    t: func,
};
