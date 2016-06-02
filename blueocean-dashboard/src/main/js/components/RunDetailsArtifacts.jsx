import React, { Component, PropTypes } from 'react';
import { EmptyStateView, FileSize } from '@jenkins-cd/design-language';
import { Icon } from 'react-material-icons-blue';
import Table from './Table';

const { object } = PropTypes;

/**
 * Displays a list of artifacts from the supplied build run property.
 */
export default class RunDetailsArtifacts extends Component {
    renderEmptyState() {
        return (
            <EmptyStateView tightSpacing>
                <p>There are no artifacts for this pipeline run.</p>
            </EmptyStateView>
        );
    }

    render() {
        const { result } = this.props;

        if (!result) {
            return null;
        }

        const { artifacts } = result;

        if (!artifacts || !artifacts.length) {
            return this.renderEmptyState();
        }

        const headers = [
            { label: 'Name', className: 'name' },
            { label: 'Size', className: 'size' },
            { label: '', className: 'actions' },
        ];

        const style = { fill: '#4a4a4a' };

        return (
            <Table headers={headers} className="artifacts-table">
                { artifacts.map(artifact => (
                    <tr key={artifact.url}>
                        <td>{artifact.name}</td>
                        <td>
                            <FileSize bytes={artifact.size} />
                        </td>
                        <td className="download">
                            <a target="_blank" title="Download the artifact" href={artifact.url}>
                                <Icon style={style} icon="file_download" />
                            </a>
                        </td>
                    </tr>
                ))}
            </Table>
        );
    }
}

RunDetailsArtifacts.propTypes = {
    result: object,
};
