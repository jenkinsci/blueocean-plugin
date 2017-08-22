// @flow

import React from 'react';

/**
 * Returns an SVG group for the "add" icon used for step list and stage graph
 */
export function getAddIconGroup(nodeRadius:number, strokeWidth:number = 0, className:string = "editor-add-node-placeholder") {

    const crossPoints = "4.67 -3.73 3.73 -4.67 0 -0.94 -3.73 -4.67 -4.67 -3.73 -0.94 0 -4.67 3.73 -3.73 4.67 0 0.94 " +
        "3.73 4.67 4.67 3.73 0.94 0";
    // TODO: ^^ get a proper "add" glyph :D

    return (
        <g>
            <circle className={className} r={nodeRadius} strokeWidth={strokeWidth}/>
            {/* TODO: ^^^ Put this into styles */}
            <g className="result-status-glyph" transform="rotate(45)">
                <polygon points={crossPoints}/>
            </g>
        </g>
    );
}

/**
 * Returns an SVG group for the "grabbable" parts of the graph / steps
 */
export function getGrabIconGroup() {

    return (
        <g className="result-status-glyph">
            <rect x="-7" y="-5" height="2" width="14"/>
            <rect x="-7" y="-1" height="2" width="14"/>
            <rect x="-7" y="3" height="2" width="14"/>
        </g>
    );
}

/**
 * Returns an SVG group for the "delete" icon
 */
export function getDeleteIconGroup(nodeRadius:number) {

    return (
        <g>
            <circle r={nodeRadius} fill="#ff4422" stroke="none"/>
            {/* TODO: ^^^ Put this into styles */}
            <g className="result-status-glyph">
                <rect x="-6" y="-1" height="2" width="12"/>
            </g>
        </g>
    );
}
