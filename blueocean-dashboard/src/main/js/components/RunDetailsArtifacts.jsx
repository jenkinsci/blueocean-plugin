import React, { Component, PropTypes } from 'react';
import { EmptyStateView, FileSize, Table } from '@jenkins-cd/design-language';
import { Icon } from 'react-material-icons-blue';
import Markdown from 'react-remarkable';
import { observer } from 'mobx-react';
import { observable, action} from 'mobx';
import mobxUtils from 'mobx-utils';
/**
 * Displays a list of artifacts from the supplied build run property.
 */
@observer
export default class RunDetailsArtifacts extends Component {
    componentWillMount() {
        const { result } = this.props;
        if (result) {
            this.artifacts = mobxUtils.fromPromise(this.context.activityService.fetchArtifacts(result._links.self.href));
        }
    }

    componentWillUnmount() {
        this.artifacts = null;
    }

    render() {
        const { result, t } = this.props;

        if (!result || !this.artifacts) {
            return null;
        }
        switch (this.artifacts.state) {
        case mobxUtils.PENDING: return <div>Loading</div>;
        case mobxUtils.REJECTED: return <div>Not found</div>;
        default:
        }

        const artifacts = this.artifacts.value.artifacts;
       
        if (!artifacts || !artifacts.length) {
            return (<EmptyStateView tightSpacing>
                <Markdown>
                    {t('EmptyState.artifacts', { defaultValue: 'There are no artifacts for this pipeline run.\n\n' })}
                </Markdown>
            </EmptyStateView>);
        }

        const headers = [
            { label: t('rundetail.artifacts.header.path', { defaultValue: 'Path' }), className: 'name' },
            { label: t('rundetail.artifacts.header.size', { defaultValue: 'Header' }), className: 'size' },
            { label: '', className: 'actions' },
        ];

        const style = { fill: '#4a4a4a' };

        return (
            <Table headers={headers} className="artifacts-table fixed">
                { artifacts.map(artifact => (
                    <tr key={artifact.url}>
                        <td>{artifact.path}</td>
                        <td>
                            <FileSize bytes={artifact.size} />
                        </td>
                        <td className="download">
                            <a target="_blank" title={t('rundetail.artifacts.button.download', { defaultValue: 'Download the artifact' })} href={artifact.url}>
                                <Icon style={style} icon="file_download" />
                            </a>
                        </td>
                    </tr>
                ))}
            </Table>
        );
    }
}

const { func, object } = PropTypes;

RunDetailsArtifacts.contextTypes = {
    activityService: object.isRequired,
};

RunDetailsArtifacts.propTypes = {
    result: object,
    t: func,
};
