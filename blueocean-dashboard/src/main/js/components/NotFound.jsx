import React from 'react';
import Fullscreen from './Fullscreen';
import { Link } from 'react-router';

/**
 * Simple component to render a fullscreen 404 page
 */
export default () => (
    <Fullscreen className="not-found">
        <div className="message-box">
            <h3>Page not found (404)</h3>
            <div className="message">Jenkins could not find the page you were looking for. Check the URL for errors or press the back button.</div>
            <div className="actions"><Link to="/" className="btn btn-primary inverse">Open Dashboard</Link></div>
        </div>
    </Fullscreen>
);
