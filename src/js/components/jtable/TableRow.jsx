// @flow

import React, { Component, PropTypes, Children } from 'react';
import { Link } from 'react-router';
import { TableHeader } from '../';
import {
    TABLE_COLUMN_SPACING,
    TABLE_LEFT_RIGHT_PADDING
} from './JTable';

import type {ColumnDescription} from './JTable';

type Props = {
    onClick?: Function,
    className?: string,
    children?: ReactChildren,
    href?: string,
    linkTo?: string,
    onClick?: Function,
    columns: Array<ColumnDescription>,
    useRollover?: boolean
};

function processChildren(children: any, columns: any): Array<React$Element<any>> {

    // First filter the children to strip any falsey values dropped by logic in our parent's render() method
    const filteredChildren = [];

    Children.forEach(children, child => {
        if (child) {
            filteredChildren.push(child);
        }
    });

    const numChildren = filteredChildren.length;
    const processedColumns:Array<ColumnDescription> = Array.isArray(columns) ? columns.concat() : [];

    // Make sure we have the right number of columns
    if (processedColumns.length !== numChildren) {
        console.warn('TableRow - received', numChildren, 'children, but', processedColumns.length, 'columns!');

        // Add generic columns if there's some missing
        while (processedColumns.length < numChildren) {
            processedColumns.push({name: '', width: 100, isFlexible: true});
        }
    }

    return filteredChildren.map((child, i) => {
        const elementStyle = child.props.style || {};
        const newStyle = {...elementStyle};
        const columnDescription = processedColumns[i];

        // Calc width including "spacing" because it needs to actually be padding in order to support whole-row anchors
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
            columns,
            href,
            linkTo,
            ...restProps
        } = this.props;

        const useRollOver = this.props.useRollover;
        const classNames = ['JTable-row'];

        if (className) {
            classNames.push(className);
        }

        const newChildren = processChildren(children, columns);

        let tagOrComponent = 'div';
        const props: Object = {
            ...restProps,
            className,
        };

        let rowIsALink = false;
        
        if (typeof href === 'string' && href.length > 0) {
            rowIsALink = true;
            // We switch to an <A> instead of <DIV> so the user can middle-click
            tagOrComponent = 'a';
            props.href = href;
        } else if (typeof linkTo === 'string' && linkTo.length > 0) {
            rowIsALink = true;
            // Use <Link> instead of <A> for local URLs because we don't know the base url here
            tagOrComponent = Link;
            props.to = linkTo;
        } 
        
        if (rowIsALink) {
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

        return React.createElement(tagOrComponent, props, ...newChildren);
    }
}

TableRow.propTypes = {
    onClick: PropTypes.func,
    className: PropTypes.string,
    children: PropTypes.node,
    onClick: PropTypes.func,
    href: PropTypes.string,
    linkTo: PropTypes.string,
    columns: PropTypes.array,
    useRollover: PropTypes.bool
};

export const TableHeaderRow = (props: $PropertyType<TableRow, 'props'> ) => {

    const columns = props.columns || [];
    const children = columns.map((col: ColumnDescription) => <TableHeader>{col.name}</TableHeader>);

    return React.createElement(TableRow, props, ...children);
};

TableHeaderRow.propTypes = TableRow.propTypes;

