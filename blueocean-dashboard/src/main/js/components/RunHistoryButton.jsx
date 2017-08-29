import React, { PropTypes } from 'react';

import { Icon } from '@jenkins-cd/design-language';
import { buildPipelineUrl } from '../util/UrlUtils';
import { Link } from 'react-router';

const RunHistoryButton = (props) => {
    const { pipeline, branchName, t, iconColor, hoverIconColor } = props;
    const historyButtonUrl = `${buildPipelineUrl(pipeline.organization, pipeline.fullName)}/activity?branch=${encodeURIComponent(branchName)}`;

    return (<div className="history-button-component">
        <Link to={historyButtonUrl} className="materials-icons history-button" title={t('branchdetail.actionbutton.history', { defaultValue: 'History' })} >
            <Icon size={24} icon="ActionHistory" color={iconColor} hoverColor={hoverIconColor} />
        </Link>
    </div>);
};

RunHistoryButton.propTypes = {
    pipeline: PropTypes.object.isRequired,
    branchName: PropTypes.string.isRequired,
    t: PropTypes.func,
    iconColor: PropTypes.string,
    hoverIconColor: PropTypes.string,
};

RunHistoryButton.defaultProps = {
    iconColor: '#ffffff',
    hoverIconColor: '',
};

export default RunHistoryButton;
