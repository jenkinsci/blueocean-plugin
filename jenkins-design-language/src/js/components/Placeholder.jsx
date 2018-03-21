// @flow

import React, { PropTypes } from 'react';
import { JTable } from './jtable/JTable';
import { TableRow } from './jtable/TableRow';
import { TableCell } from './jtable/TableCell';

type CellDescription = {
    text?: number,
    icon?: number,
};

type ColumnDescription = {
    width: number,
    isFlexible: boolean,
    head?: CellDescription,
    cell?: CellDescription,
};

type TableProps = {
    className: string,
    style: Object,
    columns: Array<ColumnDescription>,
    rowCount: number,
};

type TextProps = {
    style?: Object,
    size: number,
};

type IconProps = {
    style?: Object,
    size: number,
};

export class PlaceholderTable extends React.Component {
    props: TableProps;

    static defaultProps: TableProps = {
        className: '',
        style: {},
        columns: [],
        rowCount: 20,
    };

    render() {
        const { className, columns, rowCount, style } = this.props;

        const tableCols = columns.map(col => ({ width: col.width, isFlexible: col.isFlexible }));
        const heads = columns.map(col => col.head);
        const cells = columns.map(col => col.cell);

        const rowIterator = new Array(rowCount).fill('');
        const classString = `PlaceholderTable ${className}`;

        return (
            <div className={classString} style={style}>
                <JTable columns={tableCols}>
                    <TableRow columns={tableCols}>
                        {heads.map((item, index) => <TableCell key={`head-${index}`}>{createPlaceholderCell(item)}</TableCell>)}
                    </TableRow>
                    {rowIterator.map((item, index) => (
                        <TableRow key={`row-${index}`} columns={tableCols}>
                            {cells.map((item, index) => <TableCell key={`cell-${index}`}>{createPlaceholderCell(item)}</TableCell>)}
                        </TableRow>
                    ))}
                </JTable>
            </div>
        );
    }
}

PlaceholderTable.propTypes = {
    className: PropTypes.string,
    style: PropTypes.object,
    columns: PropTypes.array,
    rowCount: PropTypes.number,
};

export function PlaceholderText(props: TextProps) {
    return <div className="Placeholder-text" style={{ ...props.style, width: props.size }} />;
}

PlaceholderText.propTypes = {
    size: PropTypes.number,
    style: PropTypes.object,
};

export function PlaceholderIcon(props: IconProps) {
    const { size, style } = props;
    const rad = size / 2;

    return (
        <svg className="Placeholder-icon" width={size} height={size} style={style}>
            <circle cx={rad} cy={rad} r={rad} />
        </svg>
    );
}

PlaceholderIcon.propTypes = {
    size: PropTypes.number,
    style: PropTypes.object,
};

function createPlaceholderCell(item) {
    if (item && item.text) {
        return <PlaceholderText size={item.text} />;
    } else if (item && item.icon) {
        return <PlaceholderIcon size={item.icon} />;
    }

    return null;
}

export function PlaceholderTextCell(props: TextProps) {
    return (
        <TableCell style={props.style}>
            <PlaceholderText size={props.size} />
        </TableCell>
    );
}

PlaceholderTextCell.propTypes = {
    style: PropTypes.object,
    size: PropTypes.number,
};

export function PlaceholderIconCell(props: IconProps) {
    return (
        <TableCell style={props.style}>
            <PlaceholderIcon size={props.size} />
        </TableCell>
    );
}

PlaceholderIconCell.propTypes = {
    style: PropTypes.object,
    size: PropTypes.number,
};
