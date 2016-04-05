import React, { Component, PropTypes } from 'react';
const { string, object } = PropTypes;

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
    constructor(props) {
        super(props);
        this.state = { rotate: 90 };
        this.tick = () => this.setState({ rotate: this.state.rotate === 360 ? 45 : this.state.rotate + 45 });
    }

    componentDidMount() {
        this.timer = setInterval(this.tick, 500);
    }
    componentWillUnmount() {
        clearInterval(this.timer);
    }
    render() {
        const {
            title = 'No title',
            width = '320px',
            height = '320px',
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

        const d = describeArc(50, 50, 40, 0, 270);
        const { rotate } = this.state;


        console.log('rotate', rotate);

        return (<svg xmlns="http://www.w3.org/2000/svg"
          width={width}
          height={height}
          viewBox="0 0 100 100"
          preserveAspectRatio="xMidYMid"
        >
            <title id="title">{title}</title>
            <symbol>
                <g id="running">
                    <circle
                      cx="50"
                      cy="50"
                      r="40"
                      stroke={colors.strokes.outer}
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
                </g>
            </symbol>
            <rect
              x="0"
              y="0"
              width="100"
              height="100"
              fill={colors.backgrounds.box}
              className="bk"
            />
            <use
                xlinkHref="#running"
                x="0"
                y="0"
                transform={`rotate(${rotate} 50 50)`}
            />

        </svg>);
    }

}

SvgSpinner.propTypes = {
    title: string,
    width: string,
    height: string,
    colors: object,
};
