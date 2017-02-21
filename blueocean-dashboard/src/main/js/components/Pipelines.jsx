import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { Page, Table } from '@jenkins-cd/design-language';
import { i18nTranslator, ContentPageHeader, AppConfig } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { documentTitle } from './DocumentTitle';
import CreatePipelineLink from './CreatePipelineLink';
import PipelineRowItem from './PipelineRowItem';
import PageLoading from './PageLoading';

import { observer } from 'mobx-react';

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

                { !pipelines || this.pager.pending && <PageLoading /> }

                <main>
                    <article>
                        { /* TODO: need to adjust Extensions to make store available */ }
                        <Extensions.Renderer
                            extensionPoint="jenkins.pipeline.list.top"
                            store={ this.context.store }
                            router={ this.context.router }
                        />
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

                        { pipelines &&
                        <button disabled={ !this.pager.hasMore } className="btn-show-more btn-secondary" onClick={ () => this.pager.fetchNextPage() }>
                            { this.pager.pending ? translate('common.pager.loading', { defaultValue: 'Loading...' }) : translate('common.pager.more', { defaultValue: 'Show more' }) }
                        </button>
                        }
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
