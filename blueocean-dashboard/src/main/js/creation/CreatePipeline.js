import React, { PropTypes } from 'react';
import { BasicDialog, DialogContent } from '@jenkins-cd/design-language';
import { Icon } from '@jenkins-cd/react-material-icons';

import { CreatePipelineScmListRenderer } from './CreatePipelineScmListRenderer';
import { CreatePipelineStepsRenderer } from './CreatePipelineStepsRenderer';
import VerticalStep from './flow2/VerticalStep';

import Extensions from '@jenkins-cd/js-extensions';
const Sandbox = Extensions.SandboxedComponent;

import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
const translate = i18nTranslator('blueocean-dashboard');
const t = translate;

export default class CreatePipeline extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            selectedProvider: null,
        };
    }

    _onSelection(selectedProvider) {
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
            <BasicDialog
              className="creation-dialog layout-medium"
              onDismiss={() => this._onExit()}
              ignoreEscapeKey
            >
                <CustomHeader onClose={() => this._onExit()} />
                <DialogContent>
                    <VerticalStep className="first-step" status={firstStepStatus}>
                        <h1>{t('creation.core.intro.scm_provider', { defaultValue: 'Where do you store your code?' })}</h1>

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
                </DialogContent>
            </BasicDialog>
        );
    }
}

CreatePipeline.contextTypes = {
    router: PropTypes.object,
};

function CustomHeader(props) {
    return (
        <div className="Dialog-header creation-header">
            <h3>{t('creation.core.header.title', { defaultValue: 'Create Pipeline' })}</h3>
            <a className="close-button" href="#" onClick={props.onClose}
              title={t('creation.core.intro.close', { defaultValue: 'Close' })}
            >
                <Icon icon="close" size={42} />
            </a>
        </div>
    );
}

CustomHeader.propTypes = {
    onClose: PropTypes.func,
};
