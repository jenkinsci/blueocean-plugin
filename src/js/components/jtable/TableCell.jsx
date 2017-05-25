// @flow

import React, { Component, PropTypes } from 'react';

/**
 * Takes the place of a <TD>
 */
export class TableCell extends Component {

    render() {

        const {
            style,
            title,
            className,
            children,
            onClick, // TODO: remove anything from here that we're not inspecting / molesting
            ...restProps
        } = this.props;

        const classNames = ['JTable-cell'];

        if (className) {
            classNames.push(className);
        }

        const outerProps = {
            ...restProps,
            className: classNames.join(' '),
            style,
            title,
            onClick
        };

        if (typeof title === 'undefined' && typeof children === 'string') {
            outerProps.title = children;
        }

        return (
            <div {...outerProps}>
                <div className="JTable-cell-contents">
                    {children}
                </div>
            </div>
        );
    }
}

TableCell.propTypes = {
    onClick: PropTypes.func,
    style: PropTypes.object,
    title: PropTypes.string,
    className: PropTypes.string,
    children: PropTypes.node
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

TableHeader.propTypes = TableCell.propTypes;
