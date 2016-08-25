import React, { Component, PropTypes } from 'react';
import { CommitHash, EmptyStateView, ReadableDate, Table } from '@jenkins-cd/design-language';

const { object } = PropTypes;

const CommitLink = (commit) => {
    if (commit.url) {
        return (<a href={commit.url}>
            <CommitHash commitId={commit.commitId} />
        </a>);
    }
    return <CommitHash commitId={commit.commitId} />;
};

const EmptyState = () => (<EmptyStateView tightSpacing>
        <p>There are no changes for this pipeline run.</p>
    </EmptyStateView>)
;

export default class RunDetailsChanges extends Component {

    render() {
        const { result } = this.props;

        if (!result) {
            return null;
        }

        const { changeSet } = result;

        if (!changeSet || !changeSet.length) {
            return <EmptyState />;
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
                        <td><CommitLink {...commit} /></td>
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
