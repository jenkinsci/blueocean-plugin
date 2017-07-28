import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { List } from '@jenkins-cd/design-language';
import waitAtLeast from '../../flow2/waitAtLeast';
import FlowStep from '../../flow2/FlowStep';
import { Fetch, UrlConfig, AppConfig } from '@jenkins-cd/blueocean-core-js';
import { Button } from '../../github/Button';
import { Page } from '@jenkins-cd/design-language';
import { ContentPageHeader } from '@jenkins-cd/blueocean-core-js';

@observer
export default class PipelineConnect extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            errorMessage: null,
            installResult: null,
            pipelines: [],
            pipeline: null,
        };
    }
    // componentWillMount() {
    //     this._fetchPipelines();
    // }

    _selectPipeline(p) {
        this.setState()({
            pipeline: p,
        });
    }

    _fetchPipelines() {
        Fetch.fetchJSON(`${UrlConfig.getJenkinsRootURL()}/blue/rest/organizations/${AppConfig.getOrganizationName()}/bitbucket-connect/pipelines/?${this.context.location.query}`)
            .then(waitAtLeast(1000))
            .then(
                (data) => {
                    this.setState({
                        installResult: 'success',
                        pipelines: data,
                    });
                },
                error => this.setState({
                    errorMessage: error,
                })
            );
    }

    _build() {

    }

    _getParameterByName(name) {
        const query = this.context.location.query;
        const regex = new RegExp(`[?&]${name}(=([^&#]*)|&|#|$)`);
        const results = regex.exec(query);
        if (!results) return null;
        if (!results[2]) return '';
        return decodeURIComponent(results[2].replace(/\+/g, ' '));
    }

    render() {
        return (
            <Page>
                <div className="create-pipeline ac-content">
                    <ContentPageHeader>
                        <h1>Jenkins Pipelines</h1>

                    </ContentPageHeader>
                    <main>
                        <article className="content-area">
                            <FlowStep {...this.props} className="github-org-list-step layout-large" title="Jenkins Pipelines" disabled="false">
                                <List
                                    className="org-list"
                                    data={this.pipelines}
                                    onItemSelect={(idx, p) => this._selectPipeline(p)}
                                    defaultContainerClass={false}
                                />
                                <p className="instructions">
                                    Build selected pipeline. &nbsp;
                                </p>
                                <Button className="button-create-credental" status={status} onClick={() => this._build()}>Build</Button>
                            </FlowStep>
                        </article>
                    </main>
                </div>
            </Page>
        );
    }
}

PipelineConnect.contextTypes = {
    location: PropTypes.object,
};

