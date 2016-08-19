import React, { Component, PropTypes } from 'react';
import { CommitHash, EmptyStateView, ReadableDate, Table } from '@jenkins-cd/design-language';

const { object } = PropTypes;

export default class RunDetailsChanges extends Component {
    renderEmptyState() {
        return (
            <EmptyStateView tightSpacing>
                <p>There are no changes for this pipeline run.</p>
            </EmptyStateView>
        );
    }

    render() {
        const { result } = this.props;

        if (!result) {
            return null;
        }

        const { changeSet } = result;

        if (!changeSet || !changeSet.length) {
            return this.renderEmptyState();
        }

        const headers = [
            'Commit',
            { label: 'Author', className: 'author' },
            { label: 'Message', className: 'message' },
            { label: 'Date', className: 'date' },
        ];

        return (
            <Table headers={headers} className="changeset-table fixed">
                { changeSet.map(commit => (
                    <tr key={commit.commitId}>
                        <td><CommitHash commitId={commit.commitId} /></td>
                        <td>{commit.author.fullName}</td>
                        <td className="multipleLines">{commit.msg}</td>
                        <td><ReadableDate date={commit.timestamp} liveUpdate /></td>
                    </tr>
                ))}
            </Table>
        );
    }
}

RunDetailsChanges.propTypes = {
    result: object,
};
