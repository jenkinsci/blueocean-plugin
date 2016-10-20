/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import Extensions from '@jenkins-cd/js-extensions';

export class CreatePipelineScmListRenderer extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            providers: [],
        };
    }

    componentWillMount() {
        this._initialize();
    }

    _initialize() {
        Extensions.store.getExtensions(this.props.extensionPoint, (extensions) => {
            const providers = extensions.map(Provider => {
                const prov = new Provider();

                return {
                    name: prov.getDisplayName(),
                    component: prov.getComponentName(),
                };
            });

            this.setState({
                providers,
            });
        });
    }

    _onSelection(component) {
        if (this.props.onSelection) {
            this.props.onSelection(component);
        }
    }

    render() {
        return (
            <div className="scm-provider-list">
                { this.state.providers.map((provider, index) => (
                    <button
                      className="provider-button"
                      key={index}
                      onClick={() => this._onSelection(provider.component)}
                    >
                        {provider.name}
                    </button>
                ))}
            </div>
        );
    }
}

CreatePipelineScmListRenderer.propTypes = {
    extensionPoint: PropTypes.string,
    onSelection: PropTypes.func,
};
