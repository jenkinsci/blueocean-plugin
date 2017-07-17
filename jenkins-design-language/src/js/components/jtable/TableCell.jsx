// @flow

import React, { Component, PropTypes } from 'react';
import { generateLink } from './TableRow';

type Props = {
    children ?: ReactChildren,
    className ?: string,
    href ?: string,
    linkTo ?: string,
    onClick ?: Function,
    style ?: Object,
    title ?: string
};

/**
 * Takes the place of a <TD>
 */
export class TableCell extends Component {

    props: Props;

    render() {

        const {
            href,
            linkTo,
            title,
            className,
            children,
            ...restProps
        } = this.props;

        const classNames = ['JTable-cell'];

        if (className) {
            classNames.push(className);
        }

        const {
            linkProps,
            tagOrComponent
        } = generateLink('div', href, linkTo);

        const outerProps = {
            ...restProps,
            ...linkProps,
            className: classNames.join(' '),
            title,
        };

        if (typeof title === 'undefined' && typeof children === 'string') {
            outerProps.title = children;
        }

        const wrappedChildren = (
            <div className="JTable-cell-contents">
                {children}
            </div>
        );

        return React.createElement(tagOrComponent, outerProps, wrappedChildren);
    }
}

TableCell.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
    href: PropTypes.string,
    linkTo: PropTypes.string,
    onClick: PropTypes.func,
    style: PropTypes.object,
    title: PropTypes.string
};

/**
 * Takes the place of a single <TH>
 */
export const TableHeader = (props: $PropertyType<TableCell, 'props'> ) => {

    const {
        className,
        children
    } = props;

    const classNames = ['JTable-header'];

    if (className) {
        classNames.push(className);
    }

    const newProps = {
        ...props,
        children: undefined,
        className: classNames.join(' ')
    };

    return <TableCell {...newProps}>{children}</TableCell>;
};

TableHeader.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
    href: PropTypes.string,
    linkTo: PropTypes.string,
    onClick: PropTypes.func,
    style: PropTypes.object,
    title: PropTypes.string
};
