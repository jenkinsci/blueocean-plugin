import React from 'react';
import Fullscreen from './Fullscreen';
import { Link } from 'react-router';
import i18nTranslator from './i18n/i18n';

const translate = i18nTranslator('blueocean-web');

/**
 * Simple component to render a fullscreen 404 page
 */
export default () => (
    <Fullscreen className="errorscreen not-found">
        <div className="message-box">
            <h3>{translate('Not.found.heading', {
                defaultValue: 'Page not found (404)',
            })}</h3>
            <div className="message">{translate('Not.found.message', { defaultValue: 'Jenkins could not find the page you were looking for. Check the URL for errors or press the back button.' })}</div>
            <div className="actions"><Link to="/" className="btn">{translate('Open.dashboard', { defaultValue: 'Open Dashboard' })}</Link></div>
        </div>
    </Fullscreen>
);
