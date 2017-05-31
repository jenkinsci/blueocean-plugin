import React, { Component, PropTypes } from 'react';
import {
    JTable,
    TableHeaderRow,
} from '@jenkins-cd/design-language';
import { capable, ShowMoreButton } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';

import { MULTIBRANCH_PIPELINE } from '../Capabilities';
import { NoBranchesPlaceholder } from './placeholder/NoBranchesPlaceholder';
import { UnsupportedPlaceholder } from './placeholder/UnsupportedPlaceholder';

import { BranchDetailsRow } from './BranchDetailsRow';

import Extensions from '@jenkins-cd/js-extensions';

// TODO: Rename this
@observer
export class MultiBranch extends Component {

    state = {
        actionExtensionCount: 0,
    };

    componentWillMount() {
        if (this.props.pipeline && this.context.params && capable(this.props.pipeline, MULTIBRANCH_PIPELINE)) {
            const { organization, pipeline } = this.context.params;
            this.pager = this.context.pipelineService.branchPager(organization, pipeline);
        }
        this._countExtensions();
    }

    // Figure out how many extensions we have for the action buttons column so we can size it appropriately
    _countExtensions() {
        Extensions.store.getExtensions('jenkins.pipeline.branches.list.action', extensions => {
            const count = extensions && typeof(extensions.length) === 'number' ? extensions.length : 0;
            if (count !== this.state.actionExtensionCount) {
                this.setState({ actionExtensionCount: count });
            }
        });
    }

    render() {
        const { t, locale, pipeline } = this.props;
        const { actionExtensionCount } = this.state;
        const actionsInRowCount = BranchDetailsRow.actionItemsCount; // Non-extension actions

        if (!capable(pipeline, MULTIBRANCH_PIPELINE)) {
            const childProps = {
                title: t('pipelinedetail.placeholder.unsupported.branches.title'),
                message: t('pipelinedetail.placeholder.unsupported.branches.message'),
                linkText: t('pipelinedetail.placeholder.unsupported.branches.linktext'),
                linkHref: t('pipelinedetail.placeholder.unsupported.branches.linkhref'),
            };

            return (<UnsupportedPlaceholder {...childProps} />);
        }

        const branches = this.pager.data;

        if (!this.pager.pending && !branches.length) {
            return <NoBranchesPlaceholder t={t} />;
        }

        const head = 'pipelinedetail.branches.header';

        const statusHeader = t(`${head}.status`, { defaultValue: 'Status' });
        const healthHeader = t(`${head}.health`, { defaultValue: 'Health' });
        const commitHeader = t(`${head}.commit`, { defaultValue: 'Commit' });
        const branchHeader = t(`${head}.branch`, { defaultValue: 'Branch' });
        const messageHeader = t(`${head}.message`, { defaultValue: 'Message' });
        const completedHeader = t(`${head}.completed`, { defaultValue: 'Completed' });

        const actionColWidth = (actionExtensionCount + actionsInRowCount) * 24;

        const columns = [
            JTable.column(60, healthHeader, false),
            JTable.column(60, statusHeader, false),
            JTable.column(170, branchHeader, false),
            JTable.column(80, commitHeader, false),
            JTable.column(380, messageHeader, true),
            JTable.column(100, completedHeader, false),
            JTable.column(actionColWidth, '', false),
        ];

        return (
            <main>
                <article>
                    <JTable columns={columns} className="multibranch-table">
                        <TableHeaderRow />
                        { branches.map(branch => (
                            <BranchDetailsRow pipeline={pipeline} key={`${branch.name}-${branch.organization}`} data={branch} t={t} locale={locale} />
                        )) }
                    </JTable>
                    <ShowMoreButton pager={this.pager} />
                </article>
                {this.props.children}
            </main>
        );
    }
}

MultiBranch.contextTypes = {
    config: PropTypes.object.isRequired,
    params: PropTypes.object.isRequired,
    pipelineService: PropTypes.object.isRequired,
};

MultiBranch.propTypes = {
    children: PropTypes.any,
    t: PropTypes.func,
    locale: PropTypes.string,
    pipeline: PropTypes.object,
};

export default MultiBranch;
