import React, { Component, PropTypes } from 'react';
const { string, object, number } = PropTypes;

function polarToCartesian(centerX, centerY, radius, angleInDegrees) {
    const angleInRadians = (angleInDegrees - 90) * Math.PI / 180.0;

    return {
        x: centerX + (radius * Math.cos(angleInRadians)),
        y: centerY + (radius * Math.sin(angleInRadians)),
    };
}

function describeArc(x, y, radius, startAngle, endAngle) {
    const start = polarToCartesian(x, y, radius, endAngle);
    const end = polarToCartesian(x, y, radius, startAngle);

    const arcSweep = endAngle - startAngle <= 180 ? '0' : '1';

    const d = [
        'M', start.x, start.y,
        'A', radius, radius, 0, arcSweep, 0, end.x, end.y,
    ].join(' ');

    return d;
}

export default class SvgSpinner extends Component {
    render() {
        const {
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
        const d = describeArc(50, 50, 40, 0, rotate);

        return (<svg xmlns="http://www.w3.org/2000/svg"
          width={width}
          height={height}
          viewBox="0 0 100 100"
          preserveAspectRatio="xMidYMid"
        >
            <title id="title">{title}</title>
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
              id="arc1"
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
    height: string,
    percentage: number,
    colors: object,
};
