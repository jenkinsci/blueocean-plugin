import React, { PropTypes } from 'react';
import { Page } from '@jenkins-cd/design-language';
import { ContentPageHeader, i18nTranslator, Security, User } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';

import { ClassicCreationLink } from './ClassicCreationLink';
import { CreatePipelineScmListRenderer } from './CreatePipelineScmListRenderer';
import { CreatePipelineStepsRenderer } from './CreatePipelineStepsRenderer';
import VerticalStep from './flow2/VerticalStep';
import loadingIndicator from '../LoadingIndicator';
import StepStatus from './flow2/FlowStepStatus';


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

    _isCreationDisabled() {
        const user = User.current();
        return Security.isSecurityEnabled() && user && !user.permissions.pipeline.create();
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

    _onNavigatePipelines() {
        this._onExit({ url: '/pipelines' });
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
        const firstStepStatus = this.state.selectedProvider ? StepStatus.COMPLETE : StepStatus.ACTIVE;
        const creationEnabled = !this._isCreationDisabled();

        return (
            <Page>
                <div className="create-pipeline">
                    <ContentPageHeader>
                        <h1>{t('creation.core.header.title')}</h1>

                        <ClassicCreationLink />
                    </ContentPageHeader>
                    { creationEnabled &&
                    <main>
                        <article className="content-area">
                            <VerticalStep className="first-step" status={firstStepStatus}>
                                <h1>{t('creation.core.intro.scm_provider')}</h1>

                                <CreatePipelineScmListRenderer
                                    extensionPoint="jenkins.pipeline.create.scm.provider"
                                    onSelection={(provider) => this._onSelection(provider)}
                                    selectedProvider={this.state.selectedProvider}
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
                    }
                    { !creationEnabled &&
                    <main>
                        <article className="content-area">
                            <VerticalStep className="first-step" status={StepStatus.ERROR}>
                                <h1>{t('creation.core.intro.invalid_permission_title')}</h1>

                                <button onClick={() => this._onNavigatePipelines()}>
                                    {t('creation.core.intro.invalid_permission_button')}
                                </button>
                            </VerticalStep>
                        </article>
                    </main>
                    }
                </div>
            </Page>
        );
    }
}

CreatePipeline.contextTypes = {
    router: PropTypes.object,
};
