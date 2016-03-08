import React, {Component} from 'react';

export class WeatherIcon extends Component {
    render() {
        let failpc = parseInt(this.props.score) || 0;
        let classNames = this.getClassNames(failpc);

        return <span className={classNames}></span>;
    }

    getClassNames(failpc) {
        if (failpc < 21) return "weather-icon weather-sunny";
        if (failpc < 41) return "weather-icon weather-partially-sunny";
        if (failpc < 61) return "weather-icon weather-cloudy";
        if (failpc < 81) return "weather-icon weather-raining";
        return "weather-icon weather-storm";
    }
}
