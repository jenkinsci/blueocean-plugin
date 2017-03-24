// @flow

import React, { Component, PropTypes, Children } from 'react';
import { TableHeader } from './';
import {
    TABLE_COLUMN_SPACING,
    TABLE_LEFT_RIGHT_PADDING
} from './JTable';

import type {ColumnDescription} from './JTable';

type Props = {
    className?: string,
    children?: ReactChildren,
    href?: string,
    onClick?: Function,
    columns: Array<ColumnDescription>,
    useRollover?: boolean
};

function processColumns(columns: any, numChildren: number) {

    const processed = Array.isArray(columns) ? columns.concat() : [];

    // Make sure we have the right number of columns
    if (processed.length !== numChildren) {
        console.warn('TableRow - received', numChildren, 'children, but', processed.length, 'columns!');

        // Add generic columns if there's some missing
        while (processed.length < numChildren) {
            processed.push({name: '', width: 100, isFlexible: true});
        }
    }

    return processed;
}

/**
 * A table row, stand-in for <TR>. Can take a href="" attribute, which will render the row as an anchor instead of a
 * div.
 */
export class TableRow extends Component {

    props: Props;

    constructor(props: Props) {
        super(props);
    }

    render() {

        const {
            className,
            children,
            href,
            onClick,
        } = this.props;

        const useRollOver = this.props.useRollover;
        const classNames = ['JTable-row'];

        if (className) {
            classNames.push(className);
        }

        const numChildren = Children.count(children);
        const columns = processColumns(this.props.columns, numChildren);

        const newChildren = Children.map(children, (child, i) => {

            const elementStyle = child.props.style || {};
            const newStyle = {...elementStyle};
            const columnDescription = columns[i];

            // Calc width including "spacing" because it needs to actually be padding for whole-row anchors
            let colWidth = columnDescription.width;
            if (i === 0 || i === numChildren - 1) {
                colWidth += TABLE_LEFT_RIGHT_PADDING + (TABLE_COLUMN_SPACING / 2);
            } else {
                colWidth += TABLE_COLUMN_SPACING;
            }
            newStyle.flexBasis = colWidth;

            // Add or remove space on flexible columns in proportion to comparitive widths
            newStyle.flexGrow = newStyle.flexShrink = columnDescription.isFlexible ? colWidth : 0;

            return React.cloneElement(child, {style: newStyle});
        });

        let tagName = 'div';
        const props = {
            onClick,
            href,
            className
        };

        if (typeof href === 'string' && href.length > 0) {
            // We switch to an <A> instead of <DIV> so the user can middle-click
            tagName = 'a';
            classNames.push('JTable-row--href');

            if (useRollOver !== false) {
                // ^ Explicitly setting to false (not falsy) will disable rollover
                classNames.push('JTable-row--rollOver');
            }
        } else {
            if (useRollOver) {
                classNames.push('JTable-row--rollOver');
            }
        }

        props.className = classNames.join(' ');

        return React.createElement(tagName, props, ...newChildren);
    }
}

TableRow.propTypes = {
    className: PropTypes.string,
    children: PropTypes.node,
    onClick: PropTypes.func,
    href: PropTypes.string,
    columns: PropTypes.array,
    useRollover: PropTypes.bool
};

export const TableHeaderRow = (props: $PropertyType<TableRow, 'props'> ) => {

    const columns = props.columns || [];
    const children = columns.map((col: ColumnDescription) => <TableHeader>{col.name}</TableHeader>);

    return React.createElement(TableRow, props, ...children);
};

TableHeaderRow.propTypes = TableRow.propTypes;

