import React from 'react';

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

        // Find next escape code
        i2 = input.indexOf('\x1b[', i1);

        if (i2 === -1) {
            // No more escape codes
            break;
        }

        i1 = i2 + 1; // TODO: extract code and set i1 to the start of the rest of the string
    }

    if (i1 < len) {
        result.push(input.substr(i1));
    }

    return result;
}

