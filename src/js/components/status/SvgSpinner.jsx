// @flow

import React, { Component, PropTypes } from 'react';
import {describeArcAsPath, polarToCartesian} from '../SVG';
const { string, object, number } = PropTypes;

export default class SvgSpinner extends Component {
    render() {
        const {
            result = 'failure',
            percentage = 12.5,
            title = 'No title',
            width = '32px',
            height = '32px',
            colors = {
                backgrounds: {
                    box: 'none',
                    outer: 'none',
                },
                strokes: {
                    outer: '#a9c6e6',
                    path: '#4a90e2',

                },
            },
        } = this.props;

        const rotate = percentage / 100 * 360;
        const d = describeArcAsPath(50, 50, 40, 0, rotate);

        return (<svg xmlns="http://www.w3.org/2000/svg"
          className={result === 'queued' ? 'spin' : ''}
          width={width}
          height={height}
          viewBox="0 0 100 100"
          preserveAspectRatio="xMidYMid"
        >
            <title>{title}</title>
            <rect
              x="0"
              y="0"
              width="100"
              height="100"
              fill={colors.backgrounds.box}
              className="bk"
            />
            <circle
              cx="50"
              cy="50"
              r="40"
              stroke={rotate === 360 ? colors.strokes.path : colors.strokes.outer}
              fill={colors.backgrounds.outer}
              strokeWidth="10"
              strokeLinecap="round"
            />
            <path
              className={result}
              fill="none"
              stroke={colors.strokes.path}
              strokeWidth="10"
              d={d}

            />
        </svg>);
    }
}

SvgSpinner.propTypes = {
    title: string,
    width: string,
    result: string,
    height: string,
    percentage: number,
    colors: object,
};
