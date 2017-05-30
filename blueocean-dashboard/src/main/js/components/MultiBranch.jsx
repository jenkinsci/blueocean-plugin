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

import {BranchDetailsRow} from './BranchDetailsRow';

const { object, string, any, func } = PropTypes;

// TODO: Rename this
@observer
export class MultiBranch extends Component {
    componentWillMount() {
        if (this.props.pipeline && this.context.params && capable(this.props.pipeline, MULTIBRANCH_PIPELINE)) {
            const { organization, pipeline } = this.context.params;
            this.pager = this.context.pipelineService.branchPager(organization, pipeline);
        }
    }

    render() {
        const { t, locale, pipeline } = this.props;

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

        const actionColWidth = 80; // TODO: Calc based on extensions

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
    config: object.isRequired,
    params: object.isRequired,
    pipelineService: object.isRequired,
};

MultiBranch.propTypes = {
    children: any,
    t: func,
    locale: string,
    pipeline: object,
};

export default MultiBranch;
