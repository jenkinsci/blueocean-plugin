import React, {Component} from 'react';

function getClassNames(successpc) {
    if (successpc < 21) return "weather-icon weather-storm";
    if (successpc < 41) return "weather-icon weather-raining";
    if (successpc < 61) return "weather-icon weather-cloudy";
    if (successpc < 81) return "weather-icon weather-partially-sunny";
    return "weather-icon weather-sunny";
}

export class WeatherIcon extends Component {
    render() {
        let successpc = parseInt(this.props.score) || 0;
        let classNames = getClassNames(successpc);

        return <span className={classNames}/>;
    }
}
