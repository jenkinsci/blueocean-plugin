import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import {
    CommitId,
    PlaceholderTable,
    ReadableDate,
    JTable,
    TableHeaderRow,
    TableRow,
    TableCell,
} from '@jenkins-cd/design-language';
import Icon from './placeholder/Icon';
import { PlaceholderDialog } from './placeholder/PlaceholderDialog';


function NoChangesPlaceholder(props) {
    const { t } = props;

    const columns = [
        { width: 750, isFlexible: true, head: { text: 40 }, cell: { text: 150 } },
        { width: 90, head: { text: 50 }, cell: { text: 60 } },
        { width: 30, head: {}, cell: { icon: 20 } },
    ];

    const content = {
        icon: Icon.EDIT,
        title: t('rundetail.changes.placeholder.title'),
    };

    return (
        <div className="RunDetailsEmpty NoChanges">
            <PlaceholderTable columns={columns} />
            <PlaceholderDialog width={265} content={content} />
        </div>
    );
}

NoChangesPlaceholder.propTypes = {
    t: PropTypes.func,
};


function AddIssuesLinksToMsg(props) {
    const { commit } = props;

    let commitMsg = commit.msg;
    const commitMsgWithIssues = [];

    if (commit.issues) {
        let issuesIdString = '';
        const issuesObj = {};

        for (const issue of commit.issues) {
            issuesIdString += `${issue.id}|`;
            issuesObj[issue.id] = issue.url;
        }

        if (issuesIdString) {
            const issuesRegExpString = new RegExp(`(${issuesIdString.slice(0, -1)})`, 'gi');

            for (let commitMsgPart of commitMsg.split(issuesRegExpString)) {
                if (issuesObj[commitMsgPart]) {
                    commitMsgWithIssues.push(<a href={issuesObj[commitMsgPart]} target="_blank">{commitMsgPart}</a>);
                } else {
                    if (commitMsgPart) {
                        commitMsgWithIssues.push(commitMsgPart);
                    }
                }
            }
        }

        return (<span>{commitMsgWithIssues}</span>);
    }

    return (<span>{commitMsg}</span>);
}

AddIssuesLinksToMsg.propTypes = {
    commit: PropTypes.object,
};

export default class RunDetailsChanges extends Component {

    render() {
        const { result, t, locale } = this.props;

        if (!result) {
            return null;
        }

        const { changeSet } = result;

        if (!changeSet || !changeSet.length) {
            return <NoChangesPlaceholder t={t} />;
        }

        const commitLabel = t('rundetail.changes.header.commit', { defaultValue: 'Commit' });
        const authorLabel = t('rundetail.changes.header.author', { defaultValue: 'Author' });
        const messageLabel = t('rundetail.changes.header.message', { defaultValue: 'Message' });
        const dateLabel = t('rundetail.changes.header.date', { defaultValue: 'Date' });

        const columns = [
            JTable.column(100, commitLabel),
            JTable.column(100, authorLabel),
            JTable.column(500, messageLabel, true),
            JTable.column(100, dateLabel),
        ];

        return (
            <JTable columns={columns} className="changeset-table">
                <TableHeaderRow />
                { changeSet.map(commit => (
                    <TableRow key={commit.commitId}>
                        <TableCell><CommitId commitId={commit.commitId} url={commit.url} /></TableCell>
                        <TableCell>{commit.author.fullName}</TableCell>
                        <TableCell className="multipleLines"><AddIssuesLinksToMsg commit={commit} /></TableCell>
                        <TableCell>
                            <ReadableDate date={commit.timestamp}
                                          liveUpdate
                                          locale={locale}
                                          shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                                          longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
                            />
                        </TableCell>
                    </TableRow>
                ))}
            </JTable>
        );
    }
}

const { func, object, string } = PropTypes;

RunDetailsChanges.propTypes = {
    result: object,
    locale: string,
    t: func,
};
