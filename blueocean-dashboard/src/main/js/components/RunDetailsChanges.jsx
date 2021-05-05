import React, { Component, PropTypes } from 'react';
import { observer } from 'mobx-react';
import { CommitId, PlaceholderTable, ReadableDate, JTable, TableHeaderRow, TableRow, TableCell } from '@jenkins-cd/design-language';
import Icon from './placeholder/Icon';
import { PlaceholderDialog } from './placeholder/PlaceholderDialog';
import LinkifiedText from './LinkifiedText';
import { ShowMoreButton } from '@jenkins-cd/blueocean-core-js';

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

@observer
export default class RunDetailsChanges extends Component {
    componentWillMount() {
        this._fetchChangeSet(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._fetchChangeSet(nextProps);
    }

    _fetchChangeSet(props) {
        if (props.result && props.result._links && props.result._links && props.result._links.self) {
            this.pager = this.context.activityService.changeSetPager(`${props.result._links.self.href}changeSet/`);
        }
    }

    render() {
        const { t, locale } = this.props;

        if (!this.pager) {
            return null;
        }

        if (this.pager.pendingD) {
            return <NoChangesPlaceholder t={t} />;
        }

        const changeSet = this.pager.data;

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

        const changeSetSplitBySource = [];
        changeSet.map(commit => {
            if (changeSetSplitBySource[commit.checkoutCount] === undefined) {
                changeSetSplitBySource[commit.checkoutCount] = [];
            }
            changeSetSplitBySource[commit.checkoutCount].push(commit);
        });

        return (
            <div>
                {changeSetSplitBySource.map((changeSet, changeSetIdx) => (
                    <JTable columns={columns} className="changeset-table" key={changeSetIdx}>
                        <TableHeaderRow />
                        {changeSet.map(commit => (
                            <TableRow key={commit.commitId}>
                                <TableCell>
                                    <CommitId commitId={commit.commitId} url={commit.url} />
                                </TableCell>
                                <TableCell>{commit.author.fullName}</TableCell>
                                <TableCell className="multipleLines">
                                    <LinkifiedText text={commit.msg} partialTextLinks={commit.issues} />
                                </TableCell>
                                <TableCell>
                                    <ReadableDate
                                        date={commit.timestamp}
                                        liveUpdate
                                        locale={locale}
                                        shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                                        longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
                                    />
                                </TableCell>
                            </TableRow>
                        ))}
                    </JTable>
                ))}
                <ShowMoreButton pager={this.pager} />
            </div>
        );
    }
}

RunDetailsChanges.propTypes = {
    result: PropTypes.object,
    locale: PropTypes.string,
    t: PropTypes.func,
    params: PropTypes.any,
    pipeline: PropTypes.object,
    results: PropTypes.object,
};

RunDetailsChanges.contextTypes = {
    params: PropTypes.object.isRequired,
    activityService: PropTypes.object.isRequired,
};
