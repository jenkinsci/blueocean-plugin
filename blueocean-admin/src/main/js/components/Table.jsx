import React, {Component, PropTypes} from 'react';

export default class Table extends Component {

  render() {
    const { headers } = this.props;
    return (<table>
      { headers && <thead>
        <tr>
          { headers.map((column) => <th key={column}>{column}</th>) }
        </tr>
      </thead> }

      <tbody>
        {this.props.children}
      </tbody>

    </table>);
  }
}

Table.propTypes = {
  headers: PropTypes.array
};
