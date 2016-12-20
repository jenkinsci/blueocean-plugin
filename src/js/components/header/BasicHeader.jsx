// @flow

import React, { PropTypes } from 'react';

import type { Result } from '../status/StatusIndicator';


function classNameForStatusColor(statusColor?: Result): string {
    switch (statusColor) {
        case null:
        case undefined:
        case "":
            return "BasicHeader--default";
        case "success":
            return "BasicHeader--success";
        case "failure":
            return "BasicHeader--failure";
        case "running":
            return "BasicHeader--running";
        case "queued":
        case "not_built":
            return "BasicHeader--notBuilt";
        case "unstable":
            return "BasicHeader--unstable";
        case "aborted":
            return "BasicHeader--aborted";
        case "paused":
            return "BasicHeader--paused";
    }

    return "BasicHeader--unknown";
}

type Props = {
    className?: string,
    children?: ReactChildren,
    statusColor?: Result
};

export const BasicHeader = (props: Props) => {

    const classNames = ["BasicHeader", classNameForStatusColor(props.statusColor)];

    if (props.className) {
        classNames.push(props.className);
    }

    return (
        <section className={classNames.join(' ')}>
            {props.children}
        </section>
    );
};

BasicHeader.propTypes = {
    children: PropTypes.node,
    statusColor: PropTypes.string
};
