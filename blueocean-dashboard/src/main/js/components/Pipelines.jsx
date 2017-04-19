import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { Page, Table } from '@jenkins-cd/design-language';
import { i18nTranslator, ContentPageHeader, AppConfig, ShowMoreButton } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { observer } from 'mobx-react';

import { documentTitle } from './DocumentTitle';
import CreatePipelineLink from './CreatePipelineLink';
import PipelineRowItem from './PipelineRowItem';
import { DashboardPlaceholder } from './placeholder/DashboardPlaceholder';

const translate = i18nTranslator('blueocean-dashboard');

@observer
export class Pipelines extends Component {
    componentWillMount() {
        this._initPager(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._initPager(nextProps);
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

        const orgLink = organization ?
            <Link
                to={ `organizations/${organization}` }
                query={ location.query }
            >
                { organization }
            </Link> : '';

        const showPipelineList = !this.pager.pending && pipelines && pipelines.length > 0;
        const showEmptyState = !this.pager.pending && (!pipelines || !pipelines.length);

        const headers = [
            { label: translate('home.pipelineslist.header.name', { defaultValue: 'Name' }), className: 'name-col' },
            translate('home.pipelineslist.header.health', { defaultValue: 'Health' }),
            translate('home.pipelineslist.header.branches', { defaultValue: 'Branches' }),
            translate('home.pipelineslist.header.pullrequests', { defaultValue: 'PR' }),
            { label: '', className: 'actions-col' },
        ];
        this.props.setTitle('Jenkins Blue Ocean');

        return (
            <Page>
                <ContentPageHeader>
                    <div className="u-flex-grow">
                        <Extensions.Renderer extensionPoint="jenkins.pipeline.header">
                            <h1>
                                <Link to="/" query={ location.query }>
                                    { translate('home.header.dashboard', { defaultValue: 'Dashboard' }) }
                                </Link>
                                { organization && ' / ' }
                                { organization && orgLink }
                            </h1>
                        </Extensions.Renderer>
                    </div>
                    <Extensions.Renderer extensionPoint="jenkins.pipeline.create.action">
                        <CreatePipelineLink />
                    </Extensions.Renderer>
                </ContentPageHeader>
                <main>
                    <article>
                        <Extensions.Renderer
                            extensionPoint="jenkins.pipeline.list.top"
                            store={ this.context.store }
                            router={ this.context.router }
                        />
                        { showEmptyState && <DashboardPlaceholder t={translate} /> }
                        { showPipelineList &&
                        <Table
                            className="pipelines-table"
                            headers={ headers }
                        >
                            { pipelines &&
                            pipelines.map(pipeline => {
                                const key = pipeline._links.self.href;
                                return (
                                    <PipelineRowItem
                                        t={ translate }
                                        key={ key } pipeline={ pipeline }
                                        showOrganization={ AppConfig.showOrg() }
                                    />
                                );
                            })
                            }
                        </Table>
                        }

                        { pipelines && <ShowMoreButton pager={this.pager} /> }
                    </article>
                </main>
            </Page>
        );
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
