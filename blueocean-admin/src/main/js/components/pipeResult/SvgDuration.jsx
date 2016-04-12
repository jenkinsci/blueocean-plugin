import React, { Component, PropTypes } from 'react';
const { string } = PropTypes;

export default class SvgDuration extends Component {
    render() {
        const {
            width = '18px',
            height = '18px',
            colors = {
                backgrounds: '#fff',
            },
            } = this.props;

        return (<svg xmlns="http://www.w3.org/2000/svg"
          width={width}
          height={height}
          viewBox="0 0 18 18"
        >
            <path
              fill="none"
              d="M0,0 L20,0 L20,20 L0,20 L0,0 Z" />
            <path
              d="M13.5333333,6.46666667 C12.5583333,5.49166667 11.2833333,5 10,5 L10,10 L6.46666667,13.5333333 C8.41666667,15.4833333 11.5833333,15.4833333 13.5416667,13.5333333 C15.4916667,11.5833333 15.4916667,8.41666667 13.5333333,6.46666667 L13.5333333,6.46666667 Z M10,1.66666667 C5.4,1.66666667 1.66666667,5.4 1.66666667,10 C1.66666667,14.6 5.4,18.3333333 10,18.3333333 C14.6,18.3333333 18.3333333,14.6 18.3333333,10 C18.3333333,5.4 14.6,1.66666667 10,1.66666667 L10,1.66666667 Z M10,16.6666667 C6.31666667,16.6666667 3.33333333,13.6833333 3.33333333,10 C3.33333333,6.31666667 6.31666667,3.33333333 10,3.33333333 C13.6833333,3.33333333 16.6666667,6.31666667 16.6666667,10 C16.6666667,13.6833333 13.6833333,16.6666667 10,16.6666667 L10,16.6666667 Z"
              fill={colors.backgrounds}
              opacity="0.82" />
        </svg>);
    }
}

SvgDuration.propTypes = {
    result: string,
    width: string,
    height: string,
};
