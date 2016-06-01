import React, { Component, PropTypes } from 'react';

const { array, node, string } = PropTypes;

/**
 * Renders a simple HTML table with optional header elements.
 *
 * Properties:
 * "children": one or more TR elements
 * "headers": an array of Strings to render, or
 *            an array of Objects with shape: { label:String, className:String }
 *
 * To set explicit column widths, specify className for the header elements and
 * specify specify className="fixed" on the Table component to use table-layout: fixed.
 */
export class Table extends Component {

    getKey(column) {
        if (typeof column === 'string') {
            return column;
        }
        return column.label;
    }

    getLabel(column) {
        if (typeof column === 'string') {
            return column;
        }
        return column.label;
    }

    getClass(column) {
        if (typeof column === 'string') {
            return null;
        }
        return column.className;
    }

    render() {
        const { headers, children } = this.props;
        const className = 'jdl-table' + (this.props.className ? ` ${this.props.className}` : '');

        return (
            <table className={className}>
            { headers &&
                <thead>
                    <tr>
                        {
                            headers.map((column) =>
                                <th key={this.getKey(column)} className={this.getClass(column)}>
                                  {this.getLabel(column)}
                                </th>)
                        }
                    </tr>
                </thead>
            }
            { headers ? (
                <tbody>{children}</tbody>
            ) : {
                children
            }}
            </table>
        );
    }
}

Table.propTypes = {
    headers: array,
    children: node,
    className: string,
};
