import React from 'react';

// TODO: DOCS once the results are locked down
export function parseEscapeCode(escapeCode) {
    const graphicsPattern = /^\u001b\[([;0-9]*)m$/; // We only care about SGR codes

    const result = {
        isSelectGraphicRendition: false,  // True when is a color / font command
        escapeCode,
    };

    const match = graphicsPattern.exec(escapeCode);

    if (match) {

        result.isSelectGraphicRendition = true;
        result.setFG = false;
        result.setBG = false;
        result.resetFG = false;
        result.resetBG = false;

        // Convert param string to array<int> with length > 1
        let params = (match[1] || '').split(';').map(str => parseInt(str || '0'));

        // Now go through the ints, decode them into bg/fg info
        for (const num of params) {

            if (num >= 30 && num <= 37) {

                result.setFG = num - 30; // Normal FG set

            } else if (num >= 40 && num <= 47) {

                result.setFG = num - 40; // Normal BG set

            } else {

                if (num === 38 || num === 0) {

                    result.resetFG = true;
                    result.setFG = false;
                }

                if (num === 48 || num === 0) {

                    result.resetBG = true;
                    result.setBG = false;
                }
            }
        }
    }

    return result;
}

/**
 * Break up a string into an array of plain strings and escape codes. Returns [input] if no codes present.
 */
export function tokenizeANSIString(input) {

    if (typeof input !== 'string') {
        return [];
    }

    const len = input.length;

    if (len === 0) {
        return [];
    }

    let i1 = 0, i2 = 0, i3 = 0; // TODO: Rename these
    let result = [];

    while (i1 < len) {

        //--------------------------------------------------------------------------
        //  Find next escape code

        i2 = input.indexOf('\x1b', i1);

        if (i2 === -1) {
            // No more escape codes
            break;
        }

        //--------------------------------------------------------------------------
        //  Capture any text between the start pointer and the escape code

        if (i2 > i1) {
            result.push(input.substring(i1, i2));
            i1 = i2; // Advance our start pointer to the beginning of the escape code
        }

        //--------------------------------------------------------------------------
        //  Find the end of the escape code (a char from 64 - 126 indicating command)

        i2 += 2; // Skip past ESC and '['

        let code = input.charCodeAt(i2);
        while (i2 < len && (code < 64 || code > 126)) {
            i2++;
            code = input.charCodeAt(i2);
        }

        //--------------------------------------------------------------------------
        //  Create token for the escape code

        result.push(parseEscapeCode(input.substring(i1, i2)));

        //--------------------------------------------------------------------------
        //  Keep looking in the rest of the string

        i1 = i2 + 1;
    }

    if (i1 < len) {
        result.push(input.substr(i1));
    }

    return result;
}

