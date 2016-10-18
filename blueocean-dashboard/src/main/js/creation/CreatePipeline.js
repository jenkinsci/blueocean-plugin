/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';

import { CreatePipelineScmListRenderer } from './CreatePipelineScmListRenderer';
import { CreatePipelineStepsRenderer } from './CreatePipelineStepsRenderer';
import { VerticalStep } from './VerticalStep';

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

export class CreatePipeline extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            activePlugin: null,
        };
    }

    _onSelection(activePlugin) {
        this.setState({
            activePlugin,
        });
    }

    _onExit() {
        this.context.router.goBack();
    }

    render() {
        return (
            <DialogPlaceholder onClose={() => this._onExit()}>
                <VerticalStep status="complete">
                    <h1>Where do you store your code?</h1>

                    <CreatePipelineScmListRenderer
                      extensionPoint="jenkins.pipeline.create.scm.provider"
                      onSelection={(plugin) => this._onSelection(plugin)}
                    />
                </VerticalStep>

                <CreatePipelineStepsRenderer
                  extensionPoint="jenkins.pipeline.create.scm.steps"
                  activePlugin={this.state.activePlugin}
                />

                <VerticalStep>
                    <h1>Completed</h1>
                </VerticalStep>
            </DialogPlaceholder>
        );
    }
}

CreatePipeline.contextTypes = {
    router: PropTypes.object,
};
