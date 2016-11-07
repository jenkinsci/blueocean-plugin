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
                    {t('EmptyState.changes')}
                </Markdown>
            </EmptyStateView>);
        }

        const head = 'rundetail.changes.header';

        const headers = [
            t(`${head}.commit`),
            { label: t(`${head}.author`), className: 'author' },
            { label: t(`${head}.message`), className: 'message' },
            { label: t(`${head}.date`), className: 'date' },
        ];

        return (
            <Table headers={headers} className="changeset-table fixed">
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
                              shortFormat={t('common.date.readable.short')}
                              longFormat={t('common.date.readable.long')}
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
