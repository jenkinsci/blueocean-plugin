/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import { BasicDialog, DialogContent } from '@jenkins-cd/design-language';
import { Icon } from 'react-material-icons-blue';

import { CreatePipelineScmListRenderer } from './CreatePipelineScmListRenderer';
import { CreatePipelineStepsRenderer } from './CreatePipelineStepsRenderer';
import VerticalStep from './VerticalStep';

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

    _onCompleteFlow() {
        this._onExit();
    }

    _onExit() {
        if (history && history.length > 2) {
            this.context.router.goBack();
        } else {
            this.context.router.push('/pipelines');
        }
    }

    render() {
        const firstStepStatus = this.state.selectedProvider ? 'complete' : 'active';

        return (
            <BasicDialog
              className="creation-dialog"
              onDismiss={() => this._onExit()}
              ignoreEscapeKey
            >
                <CustomHeader onClose={() => this._onExit()} />
                <DialogContent>
                    <VerticalStep className="first-step" status={firstStepStatus}>
                        <h1>Where do you store your code?</h1>

                        <CreatePipelineScmListRenderer
                          extensionPoint="jenkins.pipeline.create.scm.provider"
                          onSelection={(provider) => this._onSelection(provider)}
                        />
                    </VerticalStep>

                    <CreatePipelineStepsRenderer
                      selectedProvider={this.state.selectedProvider}
                      onCompleteFlow={() => this._onCompleteFlow()}
                    />
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
            <h3>Create Pipeline</h3>
            <a className="close-button" href="#" onClick={props.onClose}>
                <Icon icon="close" size={42} />
            </a>
        </div>
    );
}

CustomHeader.propTypes = {
    onClose: PropTypes.func,
};
