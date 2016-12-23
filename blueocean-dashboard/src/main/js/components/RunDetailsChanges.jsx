import React, { Component, PropTypes } from 'react';
import { CommitHash, EmptyStateView, ReadableDate, Table } from '@jenkins-cd/design-language';
import Markdown from 'react-remarkable';

const CommitLink = (commit) => {
    if (commit.url) {
        return (<a href={commit.url}>
            <CommitHash commitId={commit.commitId} />
        </a>);
    }
    return <CommitHash commitId={commit.commitId} />;
};

export default class RunDetailsChanges extends Component {

    render() {
        const { result, t, locale } = this.props;

        if (!result) {
            return null;
        }

        const { changeSet } = result;

        if (!changeSet || !changeSet.length) {
            return (<EmptyStateView tightSpacing>
                <Markdown>
                    {t('EmptyState.changes', { defaultValue: 'There are no changes for this pipeline run.\n\n' })}
                </Markdown>
            </EmptyStateView>);
        }

        const head = 'rundetail.changes.header';

        const headers = [
            t(`${head}.commit`),
            { label: t(`${head}.author`, { defaultValue: 'Author' }), className: 'author' },
            { label: t(`${head}.message`, { defaultValue: 'Message' }), className: 'message' },
            { label: t(`${head}.date`, { defaultValue: 'Date' }), className: 'date' },
        ];

        return (
            <Table headers={headers} className="changeset-table">
                { changeSet.map(commit => (
                    <tr key={commit.commitId}>
                        <td><CommitLink {...commit} /></td>
                        <td>{commit.author.fullName}</td>
                        <td className="multipleLines">{commit.msg}</td>
                        <td>
                            <ReadableDate
                              date={commit.timestamp}
                              liveUpdate
                              locale={locale}
                              shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                              longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
                            />
                        </td>
                    </tr>
                ))}
            </Table>
        );
    }
}

const { func, object, string } = PropTypes;

RunDetailsChanges.propTypes = {
    result: object,
    locale: string,
    t: func,
};
