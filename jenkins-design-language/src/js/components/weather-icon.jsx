// @flow

import React, { Component, PropTypes } from 'react';

function getStatusClassName(successpc) {
    if (successpc < 21) return 'weather-storm';
    if (successpc < 41) return 'weather-raining';
    if (successpc < 61) return 'weather-cloudy';
    if (successpc < 81) return 'weather-partially-sunny';
    return 'weather-sunny';
}

export class WeatherIcon extends Component {
    render() {
        const successpc = parseInt(this.props.score) || 0;
        const status = getStatusClassName(successpc);
        let classNames = `weather-icon ${status}`;

        if (this.props.size === "large") {
            classNames += " large-icon";
        }

        return <svg title={status} className={classNames} />;
    }

    static defaultProps = {
        size: "default"
    };

    static propTypes = {
        score: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
        size: PropTypes.string
    };
}
