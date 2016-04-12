import React, { Component, PropTypes } from 'react';
const { string } = PropTypes;

export default class SvgSuccess extends Component {
    render() {
        const {
            width = '92px',
            height = '92px',
            colors = {
                backgrounds: '#fff',
            },
            } = this.props;

        return (<svg xmlns="http://www.w3.org/2000/svg"
          width={width}
          height={height}
          viewBox="0 0 100 100"
          preserveAspectRatio="xMidYMid"
        >
            <path
              className="box"
              fill="none"
              d="M 0,0 92,0 92,92 0,92 0,0 Z" />

            <path
              fill={colors.backgrounds}
              style={{
                  opacity:0.8199999,
              }}
              d={'M 34.5,62.1 18.4,46 13.033333,51.366667 34.5,72.833333 l 46,-46 L' +
                 '75.133333,21.466667 34.5,62.1 l 0,0 z'} />
        </svg>);
    }
}

SvgSuccess.propTypes = {
    result: string,
    width: string,
    height: string,
};
