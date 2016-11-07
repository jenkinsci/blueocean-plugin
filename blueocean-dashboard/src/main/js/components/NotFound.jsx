import React from 'react';
import Fullscreen from './Fullscreen';
import { Link } from 'react-router';
import { I18n } from '@jenkins-cd/blueocean-core-js';

const t = (key) => I18n.t(key, { ns: 'jenkins.plugins.blueocean.dashboard.Messages' });
/**
 * Simple component to render a fullscreen 404 page
 */
export default () => (
    <Fullscreen className="not-found">
        <div className="message-box">
            <h3>{t('Not.found.heading')}</h3>
            <div className="message">{t('Not.found.message')}</div>
            <div className="actions"><Link to="/" className="btn btn-primary inverse">{t('Open.dashboard')}</Link></div>
        </div>
    </Fullscreen>
);
