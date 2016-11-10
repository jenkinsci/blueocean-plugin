
import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import CreatePipelineLink from './CreatePipelineLink';
import PipelineRowItem from './PipelineRowItem';
import PageLoading from './PageLoading';

import { Page, PageHeader, Table, Title } from '@jenkins-cd/design-language';
import Extensions from '@jenkins-cd/js-extensions';
import { observer } from 'mobx-react';

import { pagerService, pipelineService, PagerService } from '@jenkins-cd/blueocean-core-js';

@observer
export class Pipelines extends Component {
    componentWillMount() {
        this._initPager(this.props);
    }

    componentDidMount() {
        // TODO: re-enable this
        //const { organization = 'Jenkins' } = this.context.params;
        //this.props.setTitle(organization);
    }

    componentWillReceiveProps(nextProps) {
        this._initPager(nextProps);
    }

    _initPager(props) {
        const org = props.params.organization;
        if (org) {
            this.pager = pagerService.getPager(pipelineService.organiztionPipelinesPager(org));
        } else {
            this.pager = pagerService.getPager(pipelineService.allPipelinesPager());
        }
    }

    render() {
        const pipelines = this.pager.data;
        const { config } = this.context;
        const { organization } = this.context.params;

        const orgLink = organization ?
            <Link to={`organizations/${organization}`} className="inverse">
                {organization}
            </Link> : '';

        const headers = [
            { label: 'Name', className: 'name-col' },
            'Health',
            'Branches',
            'Pull Requests',
            { label: '', className: 'actions-col' },
        ];

        return (
            <Page>
                <PageHeader>
                    {!pipelines || pipelines.$pending && <PageLoading duration={2000} />}
                    <Title>
                        <h1>
                            <Link to="/" className="inverse">Dashboard</Link>
                            { organization && ' / ' }
                            { organization && orgLink }
                        </h1>
                        <Extensions.Renderer extensionPoint="jenkins.pipeline.create.action">
                            <CreatePipelineLink />
                        </Extensions.Renderer>
                    </Title>
                </PageHeader>
                <main>
                    <article>
                        { /* TODO: need to adjust Extensions to make store available */ }
                        <Extensions.Renderer
                          extensionPoint="jenkins.pipeline.list.top"
                          store={this.context.store}
                          router={this.context.router}
                        />
                        <Table
                          className="pipelines-table fixed"
                          headers={headers}
                        >
                            { pipelines &&
                                pipelines.map(pipeline => {
                                    const key = pipeline._links.self.href;
                                    return (
                                        <PipelineRowItem
                                          key={key} pipeline={pipeline}
                                          showOrganization={!organization}
                                        />
                                    );
                                })
                            }
                        </Table>

                        { pipelines &&
                            <button disabled={!this.pager.hasMore} className="btn-show-more btn-secondary" onClick={() => this.pager.fetchNextPage()}>
                                {this.pager.pending ? 'Loading...' : 'Show More'}
                            </button>
                        }
                    </article>
                </main>
            </Page>);
    }
}

const { array, func, object } = PropTypes;

Pipelines.contextTypes = {
    config: object,
    params: object,
    store: object,
    router: object,
    pipelinesService: object,
};

Pipelines.propTypes = {
    setTitle: func,
    pipelines: array,
};

export default Pipelines;
