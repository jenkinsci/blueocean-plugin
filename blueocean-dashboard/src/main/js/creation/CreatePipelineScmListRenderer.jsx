/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import Extensions from '@jenkins-cd/js-extensions';

const Sandbox = Extensions.SandboxedComponent;

/**
 * Displays the initial set of options to begin a creation flow from a SCM Provider.
 */
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

    customSortProviders(providers, sortedProviders, providerToSearch) {
        const providerKeys = Object.keys(providers);

        for (let i = 0; i <= providerKeys.length; i++) {
            if (providers[i] && ((providers[i].constructor.name == providerToSearch) || !providerToSearch)) {
                sortedProviders.push(providers[i]);

                delete providers[i];
                break;
            }
        }
    }

    _initialize() {
        // load and store the SCM providers that contributed the specified extension point
        Extensions.store.getExtensions(this.props.extensionPoint, (extensions) => {
            let providers = extensions.map(Provider => {
                try {
                    return new Provider();
                } catch (error) {
                    console.warn('error initializing ScmProvider', Provider, error);
                    return null;
                }
            });

            providers = providers.filter(provider => !!provider);

            let sortedProviders = [];

            this.customSortProviders(providers, sortedProviders, 'BbCloudScmProvider');
            this.customSortProviders(providers, sortedProviders, 'BbServerScmProvider');
            this.customSortProviders(providers, sortedProviders, 'GithubScmProvider');
            this.customSortProviders(providers, sortedProviders, 'GithubEnterpriseScmProvider');
            this.customSortProviders(providers, sortedProviders, 'GitScmProvider');
            this.customSortProviders(providers, sortedProviders); // add all other providers

            this.setState({
                providers: sortedProviders,
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
            <div className="scm-provider-list layout-large">
                { this.state.providers.map(provider => {
                    let defaultOption;
                    
                    try {
                        defaultOption = provider.getDefaultOption();
                    } catch (error) {
                        console.warn('error invoking getDefaultOption for Provider', provider, error);
                        return Extensions.ErrorUtils.errorToElement(error);
                    }

                    const props = {
                        onSelect: () => this._onSelection(provider),
                        isSelected: provider === this.props.selectedProvider,
                    };

                    return (
                        <Sandbox className="provider-button">
                            {React.cloneElement(defaultOption, props)}
                        </Sandbox>
                    );
                })}
            </div>
        );
    }
}

CreatePipelineScmListRenderer.propTypes = {
    extensionPoint: PropTypes.string,
    onSelection: PropTypes.func,
    selectedProvider: PropTypes.object,
};

CreatePipelineScmListRenderer.contextTypes = {
    location: PropTypes.object,
};
