import React, { Component, PropTypes } from 'react';
import { JTable, TableHeaderRow } from '@jenkins-cd/design-language';
import { capable, ShowMoreButton } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';

import TagRow from './TagRow';
import { RunsRecord } from './records';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';
import { NoTagsPlaceholder } from './placeholder/NoTagsPlaceholder';
import { UnsupportedPlaceholder } from './placeholder/UnsupportedPlaceholder';


import Extensions from '@jenkins-cd/js-extensions';

@observer
export class Tags extends Component {
    state = {
        actionExtensionCount: 0,
    };

    /* define tag pager */

    componentWillMount() {
        if (this.props.pipeline && this.props.params && capable(this.props.pipeline, MULTIBRANCH_PIPELINE)) {
            this.pager = this.context.pipelineService.tgPager(this.props.params.organization, this.props.params.pipeline);
        }
        this._countExtensions();
    }

    // Figure out how many extensions we have for the action buttons column so we can size it appropriately
    _countExtensions() {
        Extensions.store.getExtensions('jenkins.pipeline.tags.list.action', extensions => {
            const count = extensions && typeof extensions.length === 'number' ? extensions.length : 0;
            if (count !== this.state.actionExtensionCount) {
                this.setState({ actionExtensionCount: count });
            }
        });
    }

    render() {
        const { t, locale, pipeline } = this.props;
        const { actionExtensionCount } = this.state;
        const actionsInRowCount = TagRow.actionItemsCount; // Non-extension actions

        /*if (!capable(pipeline, MULTIBRANCH_PIPELINE)) {*/
        if (true) {
            const childProps = {
                title: t('pipelinedetail.placeholder.unsupported.tag.title'),
                message: t('pipelinedetail.placeholder.unsupported.tag.message'),
                linkText: t('pipelinedetail.placeholder.unsupported.tag.linktext'),
                linkHref: t('pipelinedetail.placeholder.unsupported.tag.linkhref'),
            };

            return <UnsupportedPlaceholder {...childProps} />;
        }
        const tags = this.pager.data;

        if (!this.pager.pending && !this.pager.data.length) {
            return <NoTagsPlaceholder t={t} />;
        }

        const head = 'pipelinedetail.tags.header';
        const status = t(`${head}.status`, { defaultValue: 'Status' });
        const runHeader = t(`${head}.run`, { defaultValue: 'Tag' });
        const author = t(`${head}.author`, { defaultValue: 'Author' });
        const summary = t(`${head}.summary`, { defaultValue: 'Summary' });
        const completed = t(`${head}.completed`, { defaultValue: 'Completed' });

        const columns = [
            JTable.column(60, status),
            JTable.column(60, runHeader),
            JTable.column(500, summary, true),
            JTable.column(110, author),
            JTable.column(100, completed),
            JTable.column((actionExtensionCount + actionsInRowCount) * 24, ''),
        ];

        const runRecords = tags.map(run => new RunsRecord(run));

        return (
            <main>
                <article>
                    <JTable columns={columns}>
                        <TableHeaderRow />
                        {runRecords.map((result, index) => <TagRow t={t} locale={locale} pipeline={pipeline} key={index} tg={result} />)}
                    </JTable>
                    <ShowMoreButton pager={this.pager} />
                </article>
            </main>
        );
    }
}

Tags.contextTypes = {
    config: PropTypes.object.isRequired,
    params: PropTypes.object.isRequired,
    pipelineService: PropTypes.object.isRequired,
};

Tags.propTypes = {
    locale: PropTypes.string,
    t: PropTypes.func,
    pipeline: PropTypes.object,
    params: PropTypes.object,
};

export default Tags;
