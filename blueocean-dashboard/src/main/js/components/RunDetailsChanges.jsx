import React, { Component, PropTypes } from 'react';
import { CommitHash, PlaceholderTable, ReadableDate, Table } from '@jenkins-cd/design-language';
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
                        <td><CommitHash commitId={commit.commitId} url={commit.url} /></td>
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
