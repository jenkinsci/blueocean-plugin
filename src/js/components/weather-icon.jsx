import React, {Component} from 'react';

// TODO: Remove this inline style completely once the css stuff is all sorted
const style = {
    display:"inline-block",
    width:"32px",
    height:"32px",
    background:"#fa4",
    borderRadius:"16px",
    fontSize:"70%",
    paddingTop:"10px",
    textAlign:"center"
};

export class WeatherIcon extends Component {
    render() {
        let failpc = parseInt(this.props.score) || 0;
        let classNames = this.getClassNames(failpc);
        let icon = this.getIcon(failpc);

        return <span className={classNames} style={style}>{icon}</span>;
    }

    getClassNames(failpc) {
        if (failpc < 21) return "weather-icon weather-sunny";
        if (failpc < 41) return "weather-icon weather-partially-sunny";
        if (failpc < 61) return "weather-icon weather-cloudy";
        if (failpc < 81) return "weather-icon weather-raining";
        return "weather-icon weather-storm";
    }

    getIcon(failpc) {
        if (failpc < 21) return "SNY";
        if (failpc < 41) return "PSN";
        if (failpc < 61) return "CLD";
        if (failpc < 81) return "RNG";
        return "STM";
    }
}
