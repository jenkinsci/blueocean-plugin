import React, { Component, PropTypes } from 'react';

export default class Table extends Component {

    render() {
        const { headers, children } = this.props;
        return (<table>
            { headers && <thead>
            <tr>
                { headers.map((column) => <th key={column}>{column}</th>) }
            </tr>
            </thead> }

            { headers ? (<tbody>{children}</tbody>) : { children }}

        </table>);
    }
}

Table.propTypes = {
    headers: PropTypes.array,
    children: PropTypes.array,
};
