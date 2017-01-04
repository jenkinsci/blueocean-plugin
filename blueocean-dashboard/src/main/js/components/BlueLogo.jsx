import React, { PropTypes } from 'react';

const slooshPath = 'M140,140 L84,140 L252,140 C252,201.855892 201.855892,252 140,252 ' +
    'C78.144108,252 28,201.855892 28,140 L84,140 L28,140 C28,140 56,159.6 84,159.6 ' +
    'C111.99965,159.6 139.999301,140.00049 140,140 L252,140 C252,140 224,120.4 196,120.4 ' +
    'C168.002473,120.4 140.004946,139.996538 140.000001,140 Z';

const borderPath = 'M140,280 C217.319865,280 280,217.319865 280,140 C280,62.680135 217.319865,0 140,0 ' +
    'C62.680135,0 0,62.680135 0,140 C0,217.319865 62.680135,280 140,280 Z M140,266 ' +
    'C209.587878,266 266,209.587878 266,140 C266,70.4121215 209.587878,14 140,14 ' +
    'C70.4121215,14 14,70.4121215 14,140 C14,209.587878 70.4121215,266 140,266 Z';

export const BlueOceanIcon = () => (
    <svg className="BlueOceanLogo-icon" width="24px" height="24px" viewBox="0 0 281 281" version="1.1" >
        <g stroke="none" strokeWidth="0" fillRule="evenodd">
            <path d={borderPath} />
            <path d={slooshPath} />
        </g>
    </svg>
);

export const BlueLogo = (props) => (
    <a href={props.href || '#'} className="BlueOceanLogo">
        <BlueOceanIcon />
        Blue Ocean
    </a>
);

BlueLogo.propTypes = {
    href: PropTypes.string,
};
