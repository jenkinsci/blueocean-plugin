/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';

import { CreatePipelineScmListRenderer } from './CreatePipelineScmListRenderer';
import { CreatePipelineStepsRenderer } from './CreatePipelineStepsRenderer';
import VerticalStep from './VerticalStep';

// temporary component until JDL Dialog is ready
function DialogPlaceholder(props) {
    function closeHandler() {
        if (props.onClose) {
            props.onClose();
        }
    }

    return (
        <div style={{ position: 'fixed', zIndex: 50, top: 50, left: 100, right: 100, bottom: 50, border: '1px solid black', background: '#fff' }}>
            <div style={{ position: 'absolute', zIndex: 100, top: 0, left: 0, right: 0, bottom: 0 }}>
                {props.children}
            </div>
            <a style={{ position: 'absolute', zIndex: 100, top: 10, right: 10, cursor: 'pointer' }} onClick={closeHandler}>CLOSE</a>
        </div>
    );
}

DialogPlaceholder.propTypes = {
    children: PropTypes.node,
    onClose: PropTypes.function,
};

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
        this.context.router.goBack();
    }

    render() {
        const firstStepStatus = this.state.selectedProvider ? 'complete' : 'active';

        return (
            <DialogPlaceholder onClose={() => this._onExit()}>
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
            </DialogPlaceholder>
        );
    }
}

CreatePipeline.contextTypes = {
    router: PropTypes.object,
};
