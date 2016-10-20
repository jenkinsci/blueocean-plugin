/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import Extensions from '@jenkins-cd/js-extensions';
import VerticalStep from './VerticalStep';

export class CreatePipelineStepsRenderer extends React.Component {

    constructor(props) {
        super(props);

        this._renderer = null;
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.activePlugin !== nextProps.activePlugin && this._renderer) {
            this._renderer.reloadExtensions();
        }
    }

    _filterByPlugin(extensions, nextFilter) {
        const filtered = extensions.filter(extension => (
            extension.component.indexOf(this.props.activePlugin) !== -1
        ));

        nextFilter(filtered);
    }

    render() {
        if (!this.props.activePlugin) {
            return (
                <VerticalStep className="last-step">
                    <h1>Completed</h1>
                </VerticalStep>
            );
        }

        return (
            <Extensions.Renderer
              ref={(renderer) => { this._renderer = renderer; }}
              extensionPoint={this.props.extensionPoint}
              filter={(a, b) => this._filterByPlugin(a, b)}
              onCompleteFlow={this.props.onCompleteFlow}
            />
        );
    }
}

CreatePipelineStepsRenderer.propTypes = {
    extensionPoint: PropTypes.string,
    activePlugin: PropTypes.string,
    onCompleteFlow: PropTypes.func,
};
