// @flow

import React from 'react';
import {Link} from 'react-router';
import { storiesOf } from '@kadira/storybook';
import WithContext from './WithContext';
import {
    JTable,
    TableRow,
    TableCell,
    TableHeader,
    TableHeaderRow
} from '../components';

//--------------------------------------------------------------------------
//
//  Story index
//
//--------------------------------------------------------------------------

storiesOf('JTable', module)
    .add('Basic', basic)
    .add('Manual', manual)
    .add('Component column header', headerComponent)
;


//--------------------------------------------------------------------------
//
//  Data
//
//--------------------------------------------------------------------------

const rowHeaders =
    [ "Score", "Batsman",            "For",         "Against",      "Innings", "Test", "Ground",                                    "Date"];
const rowData = [
    [ "400",   "Brian Lara",         "West Indies", "England",      "1st",     "4th",  "Antigua Recreation Ground, St John's",      "10 April 2004"    ],
    [ "380",   "Matthew Hayden",     "Australia",   "Zimbabwe",     "1st",     "1st",  "WACA Ground, Perth",                        "9 October 2003"   ],
    [ "375",   "Brian Lara",         "West Indies", "England",      "1st",     "5th",  "Antigua Recreation Ground, St John's",      "16 April 1994"    ],
    [ "374",   "Mahela Jayawardene", "Sri Lanka",   "South Africa", "1st",     "1st",  "Sinhalese S.C., Colombo",                   "27 July 2006"     ],
    [ "365",   "Garfield Sobers",    "West Indies", "Pakistan",     "1st",     "3rd",  "Sabina Park, Kingston",                     "26 February 1958" ],
    [ "364",   "Len Hutton",         "England",     "Australia",    "1st",     "5th",  "The Oval, London",                          "20 August 1938"   ],
    [ "340",   "Sanath Jayasuriya",  "Sri Lanka",   "India",        "1st",     "1st",  "R. Premadasa Stadium, Colombo",             "2 August 1997"    ],
    [ "337",   "Hanif Mohammad",     "Pakistan",    "West Indies",  "2nd",     "1st",  "Kensington Oval, Bridgetown",               "17 January 1958"  ],
    [ "336",   "Wally Hammond",      "England",     "New Zealand",  "1st",     "2nd",  "Eden Park, Auckland",                       "31 March 1933"    ],
    [ "334",   "Donald Bradman",     "Australia",   "England",      "1st",     "3rd",  "Headingley, Leeds",                         "11 July 1930"     ],
    [ "334",   "Mark Taylor",        "Australia",   "Pakistan",     "1st",     "2nd",  "Arbab Niaz Stadium, Peshawar",              "15 October 1998"  ]];
const colWidths =
    [ 40,      200,                  90,            90,             40,        40,     200,                                         130 ];

//--------------------------------------------------------------------------
//
//  Helpers
//
//--------------------------------------------------------------------------

function container(...children) {

    const style = {
        margin: "1em"
    };

    const ctx = {
        router: {
            createHref: x => "/createdHref" + x
        }
    };

    return (
        <WithContext context={ctx}>
            {React.createElement('div', {style}, ...children)}
        </WithContext>
    );
}

function rowClicked(e) {
    e.stopPropagation();
    e.preventDefault();
    console.log('rowClicked');
}

function cellClicked(e) {
    e.stopPropagation();
    e.preventDefault();
    console.log('cellClicked');
}

function renderRow(rowData) {
    const key = rowData[1] + rowData[0];
    return (
        <TableRow onClick={rowClicked} key={key}>{ rowData.map(renderCell) }</TableRow>
    );
}

function renderCell(cellValue, i) {
    return (
        <TableCell key={i}>{cellValue}</TableCell>
    );
}


//--------------------------------------------------------------------------
//
//  Story renderers
//
//--------------------------------------------------------------------------

function basic() {

    const rows = rowData.map(renderRow);
    const columns = [];

    for (let i = 0; i < colWidths.length; i++) {
        columns.push(JTable.column(colWidths[i], rowHeaders[i]));
    }

    columns[1].isFlexible = true;
    columns[6].isFlexible = true;

    return container(
        <JTable columns={columns}>
            <TableHeaderRow/>
            {rows}
        </JTable>
    );
}

function headerComponent() {

    const rows = rowData.map(renderRow);
    const columns = [];

    for (let i = 0; i < colWidths.length; i++) {
        columns.push(JTable.column(colWidths[i], rowHeaders[i]));
    }

    columns[1].isFlexible = true;
    columns[6].isFlexible = true;

    columns[6].name = <button>Button As Header</button>;

    return container(
        <JTable columns={columns}>
            <TableHeaderRow/>
            {rows}
        </JTable>
    );
}

function manual() {

    const w = 100;
    
    const columns = [
        JTable.column(w),
        JTable.column(w),
        JTable.column(w),
        JTable.column(w),
        JTable.column(w),
        JTable.column(w),
        JTable.column(w * 2)
    ];

    const style = {
        marginTop: '1em',
        marginBottom: '2em',
        width: '800px'
    };

    const style2 = {
        marginTop: '1em',
        marginBottom: '2em'
    };

    return container(
        <Link to="/relativeurl">This is in a link</Link>,
        <h3>Manual headers, row links</h3>,
        <JTable columns={columns} style={style}>
            <TableRow onClick={rowClicked}>
                <TableHeader>X</TableHeader>
                <TableHeader>Y</TableHeader>
                <TableHeader>AND</TableHeader>
                <TableHeader>OR</TableHeader>
                {
                    false // Need to make sure you can drop columns based on some logic!
                }
                <TableHeader>XOR</TableHeader>
                <TableHeader>NAND</TableHeader>
                <TableHeader>Nonsense</TableHeader>
            </TableRow>
            {
                null // Make sure we can have optional rows, as well as optional columns!
            }
            <TableRow onClick={rowClicked} href="http://www.example.org/alpha/">
                <TableCell>True</TableCell>
                <TableCell>True</TableCell>
                <TableCell>True</TableCell>
                {
                    null // Need to make sure you can drop columns based on some logic!
                }
                <TableCell>True</TableCell>
                <TableCell>False</TableCell>
                <TableCell>False</TableCell>
                <TableCell>Alpha</TableCell>
            </TableRow>
            <TableRow onClick={rowClicked} href="http://www.example.org/bravo/">
                <TableCell>True</TableCell>
                <TableCell>False</TableCell>
                <TableCell>False</TableCell>
                <TableCell>True</TableCell>

                {/* Make sure nothing breaks when you add some comments, either! */}
                
                <TableCell>True</TableCell>
                <TableCell>True</TableCell>
                <TableCell>this space intentionally left blank</TableCell>
            </TableRow>
            <TableRow onClick={rowClicked} href="http://www.example.org/charlie/">
                <TableCell>False</TableCell>
                <TableCell>True</TableCell>
                <TableCell>False</TableCell>
                <TableCell>True</TableCell>
                <TableCell>True</TableCell>
                <TableCell>True</TableCell>
                <TableCell>Charlie don't surf</TableCell>
            </TableRow>
            <TableRow onClick={rowClicked} linkTo="/app-specific-url/foo">
                <TableCell onClick={cellClicked}>False</TableCell>
                <TableCell onClick={cellClicked}>False</TableCell>
                <TableCell onClick={cellClicked}>False</TableCell>
                <TableCell onClick={cellClicked}>False</TableCell>
                <TableCell onClick={cellClicked}>False</TableCell>
                <TableCell onClick={cellClicked}>True</TableCell>
                <TableCell onClick={cellClicked}>&lt;Link&gt;</TableCell>
            </TableRow>
        </JTable>,
        <h3>Some Links, some useRollover</h3>,
        <JTable columns={columns} style={style2}>
            <TableRow>
                <TableHeader>X</TableHeader>
                <TableHeader>Y</TableHeader>
                <TableHeader>AND</TableHeader>
                <TableHeader>OR</TableHeader>
                <TableHeader>XOR</TableHeader>
                <TableHeader>NAND</TableHeader>
                <TableHeader>Comment</TableHeader>
            </TableRow>
            <TableRow href="http://www.example.org/alpha/">
                <TableCell>True</TableCell>
                <TableCell>True</TableCell>
                <TableCell>True</TableCell>
                <TableCell>True</TableCell>
                <TableCell>False</TableCell>
                <TableCell>False</TableCell>
                <TableCell>Link</TableCell>
            </TableRow>
            <TableRow href="http://www.example.org/bravo/" useRollover={false}>
                <TableCell>True</TableCell>
                <TableCell>False</TableCell>
                <TableCell>False</TableCell>
                <TableCell>True</TableCell>
                <TableCell>True</TableCell>
                <TableCell>True</TableCell>
                <TableCell>Link, useRollover=false</TableCell>
            </TableRow>
            <TableRow>
                <TableCell>False</TableCell>
                <TableCell>True</TableCell>
                <TableCell>False</TableCell>
                <TableCell>True</TableCell>
                <TableCell>True</TableCell>
                <TableCell>True</TableCell>
                <TableCell>No link</TableCell>
            </TableRow>
            <TableRow useRollover>
                <TableCell>False</TableCell>
                <TableCell>False</TableCell>
                <TableCell>False</TableCell>
                <TableCell>False</TableCell>
                <TableCell>False</TableCell>
                <TableCell>True</TableCell>
                <TableCell>No link, useRollover=true</TableCell>
            </TableRow>
        </JTable>
    );
}
