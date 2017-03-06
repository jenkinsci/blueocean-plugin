import React, { PropTypes } from 'react';
import { Page } from '@jenkins-cd/design-language';
import { ContentPageHeader, i18nTranslator, loadingIndicator } from '@jenkins-cd/blueocean-core-js';

import { ClassicCreationLink } from './ClassicCreationLink';
import { CreatePipelineScmListRenderer } from './CreatePipelineScmListRenderer';
import { CreatePipelineStepsRenderer } from './CreatePipelineStepsRenderer';
import VerticalStep from './flow2/VerticalStep';

import Extensions from '@jenkins-cd/js-extensions';
const Sandbox = Extensions.SandboxedComponent;

const t = i18nTranslator('blueocean-dashboard');

export default class CreatePipeline extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            selectedProvider: null,
        };
    }

    componentWillMount() {
        loadingIndicator.hide();
    }

    componentWillUnmount() {
        if (this.state.selectedProvider) {
            this.state.selectedProvider.destroyFlowManager();
        }
    }

    _onSelection(selectedProvider) {
        if (this.state.selectedProvider) {
            this.state.selectedProvider.destroyFlowManager();
        }

        this.setState({
            selectedProvider,
        });
    }

    _onCompleteFlow(path) {
        this._onExit(path);
    }

    _onExit({ url } = {}) {
        if (url) {
            this.context.router.replace(url);
        } else if (history && history.length <= 2) {
            this.context.router.replace('/pipelines');
        } else {
            this.context.router.goBack();
        }
    }

    render() {
        const firstStepStatus = this.state.selectedProvider ? 'complete' : 'active';

        return (
            <Page>
                <div className="create-pipeline">
                    <ContentPageHeader>
                        <h1>{t('creation.core.header.title')}</h1>

                        <ClassicCreationLink />
                    </ContentPageHeader>
                    <main>
                        <article className="content-area">
                            <VerticalStep className="first-step" status={firstStepStatus}>
                                <h1>{t('creation.core.intro.scm_provider')}</h1>

                                <CreatePipelineScmListRenderer
                                    extensionPoint="jenkins.pipeline.create.scm.provider"
                                    onSelection={(provider) => this._onSelection(provider)}
                                />
                            </VerticalStep>

                            <Sandbox>
                                <CreatePipelineStepsRenderer
                                    selectedProvider={this.state.selectedProvider}
                                    onCompleteFlow={(data) => this._onCompleteFlow(data)}
                                />
                            </Sandbox>
                        </article>
                    </main>
                </div>
            </Page>
        );
    }
}

CreatePipeline.contextTypes = {
    router: PropTypes.object,
};
