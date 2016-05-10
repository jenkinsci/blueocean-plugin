import React, { Component, PropTypes } from 'react';
import { EmptyStateView } from '@jenkins-cd/design-language';
import { Icon } from 'react-material-icons-blue';
import Table from './Table';

const { number, object } = PropTypes;

export default class RunDetailsArtifacts extends Component {
    renderEmptyState() {
        return (
            <EmptyStateView iconName="shoes">
                <h1>Ready, get set...</h1>

                <p>
                    Hmm, looks like there were no artifacts associated with this run.
                </p>
            </EmptyStateView>
        );
    }

    render() {
        const { result } = this.props;

        debugger;

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
                            {artifact.size}
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
