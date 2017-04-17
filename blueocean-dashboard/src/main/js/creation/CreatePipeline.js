import React, { PropTypes } from 'react';
import { Page } from '@jenkins-cd/design-language';
import { ContentPageHeader, i18nTranslator, loadingIndicator } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';

import { ClassicCreationLink } from './ClassicCreationLink';
import { CreatePipelineScmListRenderer } from './CreatePipelineScmListRenderer';
import { CreatePipelineStepsRenderer } from './CreatePipelineStepsRenderer';
import VerticalStep from './flow2/VerticalStep';
import StepStatus from './flow2/FlowStepStatus';
import creationUtils from './creation-status-utils';


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

        return (
            <Page>
                <div className="create-pipeline">
                    <ContentPageHeader>
                        <h1>{t('creation.core.header.title')}</h1>

                        <ClassicCreationLink />
                    </ContentPageHeader>
                    { creationUtils.isEnabled() &&
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
                    { creationUtils.isDisabled() &&
                    <main>
                        <article className="content-area">
                            <VerticalStep className="first-step" status={StepStatus.ERROR}>
                                <h1>{t('creation.core.intro.invalid_security_title')}</h1>

                                <p>
                                    <span>{t('creation.core.intro.invalid_security_message')} - </span>

                                    <a href={t('creation.core.intro.invalid_security_linkhref')} target="_blank">
                                        {t('creation.core.intro.invalid_security_linktext')}
                                    </a>
                                </p>
                            </VerticalStep>
                        </article>
                    </main>
                    }
                    { creationUtils.isHidden() &&
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
