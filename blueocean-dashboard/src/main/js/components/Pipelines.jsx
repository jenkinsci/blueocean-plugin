import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { Page, JTable, TableHeaderRow } from '@jenkins-cd/design-language';
import { i18nTranslator, ContentPageHeader, AppConfig, ShowMoreButton } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { documentTitle } from './DocumentTitle';
import CreatePipelineLink from './CreatePipelineLink';
import PipelineRowItem from './PipelineRowItem';
import { observer } from 'mobx-react';

const translate = i18nTranslator('blueocean-dashboard');


@observer
export class Pipelines extends Component {

    state = {
        actionExtensionCount: 0
    };

    componentWillMount() {
        this._initPager(this.props);
        this._countExtensions();
    }

    componentWillReceiveProps(nextProps) {
        this._initPager(nextProps);
    }

    // Figure out how many extensions we have for the action buttons column so we can size it appropriately
    _countExtensions() {
        Extensions.store.getExtensions('jenkins.pipeline.list.action', extensions => {
            const count = extensions && typeof(extensions.length) === 'number' ? extensions.length : 0;
            if (count !== this.state.actionExtensionCount) {
                this.setState({ actionExtensionCount: count });
            }
        });
    }

    _initPager(props) {
        const org = props.params.organization;
        if (org) {
            this.pager = this.context.pipelineService.organiztionPipelinesPager(org);
        } else {
            this.pager = this.context.pipelineService.allPipelinesPager();
        }
    }

    render() {
        const pipelines = this.pager.data;
        const { organization, location = { } } = this.context.params;
        const { actionExtensionCount } = this.state;

        const orgLink = organization ?
            <Link
                to={ `organizations/${organization}` }
                query={ location.query }
            >
                { organization }
            </Link> : '';

        const labelName = translate('home.pipelineslist.header.name', { defaultValue: 'Name' });
        const labelHealth = translate('home.pipelineslist.header.health', { defaultValue: 'Health' });
        const labelBranches = translate('home.pipelineslist.header.branches', { defaultValue: 'Branches' });
        const labelPullReqs = translate('home.pipelineslist.header.pullrequests', { defaultValue: 'PR' });

        const columns = [
            JTable.column(640, labelName, true),
            JTable.column(70, labelHealth),
            JTable.column(70, labelBranches),
            JTable.column(70, labelPullReqs),
            JTable.column(actionExtensionCount * 24, ''),
        ];

        const pipelineRows = pipelines && pipelines.map(pipeline => {
            const key = pipeline._links.self.href;
            return (
                <PipelineRowItem
                    t={ translate }
                    key={ key } pipeline={ pipeline }
                    showOrganization={ AppConfig.showOrg() }
                />
            );
        });

        this.props.setTitle('Jenkins Blue Ocean');

        return (
            <Page>
                <ContentPageHeader>
                    <div className="u-flex-grow">
                        <h1>
                            <Link to="/" query={ location.query }>
                                { translate('home.header.dashboard', { defaultValue: 'Dashboard' }) }
                            </Link>
                            { organization && ' / ' }
                            { organization && orgLink }
                        </h1>
                    </div>
                    <Extensions.Renderer extensionPoint="jenkins.pipeline.create.action">
                        <CreatePipelineLink />
                    </Extensions.Renderer>
                </ContentPageHeader>
                <main>
                    <article>
                        { /* FIXME: need to adjust Extensions to make store available */ }
                        <Extensions.Renderer
                            extensionPoint="jenkins.pipeline.list.top"
                            store={ this.context.store }
                            router={ this.context.router }
                        />

                        <JTable columns={columns} className="pipelines-table">
                            <TableHeaderRow />
                            { pipelineRows }
                        </JTable>

                        { pipelines && <ShowMoreButton pager={this.pager} /> }
                    </article>
                </main>
            </Page>);
    }
}

const { func, object } = PropTypes;

Pipelines.contextTypes = {
    config: object,
    params: object,
    store: object,
    router: object,
    pipelineService: object,
    location: object.isRequired, // From react-router
};

Pipelines.propTypes = {
    setTitle: func,
};

export default documentTitle(Pipelines);
