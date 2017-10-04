// @flow

import React from 'react';
import OriginalLinkify from 'linkifyjs/react';

// Makes sure we only linkify explicit URLs with expected protocols
function validateURL(value: string) {
    const explicitURL = /^(http|https|file|ftp|mailto):/i;
    return explicitURL.test(value);
}

type Props = Object; // FIXME: How do we import the type of OriginalLinkify:props?

/**
 * Thin wrapper around http://soapbox.github.io/linkifyjs/ react component that (by default) only linkifies prefixed
 * URLs. Also allows us to expose this via JDL so the rest of BO doesn't need to install it.
 *
 * NB: This is currently tested by ResultItem-spec
 *
 * @param props
 */
export const Linkify = (props: Props) => {

    const childOptions = {
        validate: validateURL,
        ...(props.options || {})
    };

    const childProps = {
        ...props,
        options: childOptions,
    };

    return (
        <OriginalLinkify {...childProps} />
    );
};

Linkify.propTypes = OriginalLinkify.propTypes;
Linkify.defaultProps = OriginalLinkify.defaultProps;
