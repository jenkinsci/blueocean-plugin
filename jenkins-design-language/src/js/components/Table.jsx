// @flow

import React, { Component, PropTypes } from 'react';

function getKey(column:string | Object) {
    if (typeof column === 'string') {
        return column;
    }
    return column.label;
}

function getLabel(column:string | Object) {
    if (typeof column === 'string') {
        return column;
    }
    return column.label;
}

function getClass(column:string | Object) {
    if (typeof column === 'string') {
        return null;
    }
    return column.className;
}

/**
 * Renders a simple HTML table with optional header elements.
 *
 * Properties:
 *
 * "children": one or more TR elements
 *
 * "headers": an array of Strings to render, or
 *            an array of Objects with shape: { label:String, className:String }
 *
 * "disableHeaderDivider": Optional, truthy if you wish to disable the HR row at the bottom of THEAD.
 *
 * "disableDefaultPadding": Optional, truthy if you wish to disable default padding added by removing "u-table-padding"
 * className from the TABLE element.
 *
 * "disableNoWrap": Optional, truthy if you wish to disable the "u-single-line" className, and allow cell contents to
 * wrap.
 *
 * "disableFixed": Optional, truthy if you wish to disable the "u-fixed" className, and use default browser fluid layout.
 *
 * Specify additional className="u-highlight-rows" for whole-row mouseover hover.
 *
 * Head / body divider will only be included automagically when using headers prop. TableDivider component can be used
 * when manually constructing a table you wish to look the same.
 */
export class Table extends Component {

    render() {
        const {
            style,
            headers,
            children,
            className,
            disableHeaderDivider,
            disableDefaultPadding,
            disableNoWrap,
            disableFixed } = this.props;

        const divider = headers && headers.length && !disableHeaderDivider ?
            <TableDivider numCols={headers.length}/> : undefined;

        const headerRowCells = headers && headers.map((column) => (
                <th key={getKey(column)} className={getClass(column)}>
                    {getLabel(column)}
                </th>
            ));

        const tableHeader = headers && (
                <thead>
                    <tr>{ headerRowCells }</tr>
                    { divider }
                </thead>
            );

        const tableClasses = ['jdl-table'];

        if (className) {
            tableClasses.push(className);
        }

        if (!disableDefaultPadding) {
            tableClasses.push('u-table-padding');
        }

        if (!disableNoWrap) {
            tableClasses.push('u-single-line');
        }

        if (!disableFixed) {
            tableClasses.push('u-fixed');
        }

        const wrapChildren = headers && children && children.type !== 'tbody';

        return (
            <table className={tableClasses.join(' ')} style={style}>
                { tableHeader }
                { wrapChildren ? <tbody>{children}</tbody> : children }
            </table>
        );
    }
}

Table.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
    style: PropTypes.any,
    headers: PropTypes.array,
    disableHeaderDivider: PropTypes.bool,
    disableDefaultPadding: PropTypes.bool,
    disableNoWrap: PropTypes.bool,
    disableFixed: PropTypes.bool
};

export const TableDivider = (props: {numCols: number}) => (
    <tr className="jdl-table-divider">
        <td colSpan={props.numCols}>
            <hr/>
        </td>
    </tr>
);

TableDivider.propTypes = {
    numCols: PropTypes.number.isRequired
};
