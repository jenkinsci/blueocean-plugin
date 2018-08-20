/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import GitScmProvider from './git/GitScmProvider';
import GithubScmProvider from './github/GithubScmProvider';
import GithubEnterpriseScmProvider from './github-enterprise/GithubEnterpriseScmProvider';
import BbCloudScmProvider from './bitbucket/cloud/BbCloudScmProvider';
import BbServerScmProvider from './bitbucket/server/BbServerScmProvider';

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
        //the provider order in the UI is the order in which they are declared in the following array
        const providersList = [BbCloudScmProvider, BbServerScmProvider, GithubScmProvider, GithubEnterpriseScmProvider, GitScmProvider];
        this._initialize(providersList);
    }

    _initialize(providersList) {
        // load and store the SCM providers
        let providers = providersList.map(Provider => {
            try {
                return new Provider();
            } catch (error) {
                console.warn('error initializing ScmProvider', Provider, error);
                return null;
            }
        });

        providers = providers.filter(provider => !!provider);

        this.setState({
            providers,
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
                {this.state.providers.map(provider => {
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

                    return <div className="provider-button">{React.cloneElement(defaultOption, props)}</div>;
                })}
            </div>
        );
    }
}

CreatePipelineScmListRenderer.propTypes = {
    onSelection: PropTypes.func,
    selectedProvider: PropTypes.object,
};

CreatePipelineScmListRenderer.contextTypes = {
    location: PropTypes.object,
};
