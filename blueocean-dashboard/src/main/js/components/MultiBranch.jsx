import React, { Component, PropTypes } from 'react';
import { Table } from '@jenkins-cd/design-language';
import { capable, ShowMoreButton } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';

import Branches from './Branches';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';
import { NoBranchesPlaceholder } from './placeholder/NoBranchesPlaceholder';
import { UnsupportedPlaceholder } from './placeholder/UnsupportedPlaceholder';


const { object, string, any, func } = PropTypes;


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
            return (<UnsupportedPlaceholder t={t} />);
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

        const headers = [
            healthHeader,
            statusHeader,
            { label: branchHeader, className: 'branch' },
            { label: commitHeader, className: 'lastcommit' },
            { label: messageHeader, className: 'message' },
            { label: completedHeader, className: 'completed' },
            { label: '', className: 'run' },
        ];

        return (
            <main>
                <article>
                    <Table className="multibranch-table u-highlight-rows u-table-lr-indents" headers={headers} disableDefaultPadding>
                        {branches.length > 0 && branches.map((branch, index) => <Branches pipeline={pipeline} key={index} data={branch} t={t} locale={locale} />)}
                    </Table>
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
