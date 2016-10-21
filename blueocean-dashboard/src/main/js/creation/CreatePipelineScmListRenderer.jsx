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
                return new Provider();
            });

            this.setState({
                providers,
            });
        });
    }

    _onSelection(provider) {
        if (this.props.onSelection) {
            this.props.onSelection(provider);
        }
    }

    render() {
        return (
            <div className="scm-provider-list">
                { this.state.providers.map(provider => {
                    const props = {
                        onSelect: () => this._onSelection(provider),
                    };

                    return React.cloneElement(provider.getDefaultOption(), props);
                })}
            </div>
        );
    }
}

CreatePipelineScmListRenderer.propTypes = {
    extensionPoint: PropTypes.string,
    onSelection: PropTypes.func,
};
