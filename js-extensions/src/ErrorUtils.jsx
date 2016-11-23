/**
 * Created by cmeyers on 10/24/16.
 */
import React from 'react';

/**
 * Convert an error to a suitable React element for display in UI.
 * @param error
 */
function errorToElement(error) {
    return (
        <div className="error">Error rendering: {error.toString()}</div>
    );
}


export default {
    errorToElement,
};
