import React from 'react';

// TODO: DOCS once the results are locked down
export function parseEscapeCode(escapeCode) {

    const result = {
        isSelectGraphicRendition: false,  // True when is a color / font command
        escapeCode,
    };

    if (escapeCode.charAt(escapeCode.length -1) === 'm') {
        result.isSelectGraphicRendition = true;
    }

    return result;
}

/**
 * Break u¹p a string into an array of plain strings and escape codes. Returns [input] if no codes present.
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

        i1 = i2 + 1; // TODO: extract code and set i1 to the start of the rest of the string
    }

    if (i1 < len) {
        result.push(input.substr(i1));
    }

    return result;
}

