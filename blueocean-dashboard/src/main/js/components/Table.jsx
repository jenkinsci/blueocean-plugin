import React, { Component, PropTypes } from 'react';

const { array, string } = PropTypes;

export default class Table extends Component {

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

        return (<table className={this.props.className}>
            { headers && <thead>
            <tr>
                {
                    headers.map((column) =>
                        <th key={this.getKey(column)} className={this.getClass(column)}>
                          {this.getLabel(column)}
                        </th>)
                }
            </tr>
            </thead> }

            { headers ? (<tbody>{children}</tbody>) : { children }}

        </table>);
    }
}

Table.propTypes = {
    headers: array,
    children: array,
    className: string,
};
