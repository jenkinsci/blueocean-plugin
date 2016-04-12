import React, { Component, PropTypes } from 'react';
const { string } = PropTypes;

export default class SvgError extends Component {
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
              d={`M72.8333333,24.5716667 L67.4283333,19.1666667 L46,40.595 L24.5716667,19.1666667 ` +
                 `L19.1666667,24.5716667 L40.595,46 L19.1666667,67.4283333 L24.5716667,72.8333333 ` +
                 `L46,51.405 L67.4283333,72.8333333 L72.8333333,67.4283333 L51.405,46 L72.8333333,24.5716667 Z`
                 }
            />
        </svg>);
    }
}

SvgError.propTypes = {
    result: string,
    width: string,
    height: string,
};
