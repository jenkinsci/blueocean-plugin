import React from 'react';
import PropTypes from 'prop-types';
import { Dropdown } from '@jenkins-cd/design-language';
import { getArg, setArg } from '../../services/PipelineMetadataService';

const timeUnits = ['SECONDS', 'MINUTES', 'HOURS', 'DAYS', 'NANOSECONDS', 'MICROSECONDS', 'MILLISECONDS'];

export default class TimeUnitPropertyInput extends React.Component {
    render() {
        const { step, type, onChange, propName } = this.props;
        return (
            <div>
                <label className="form-label">{type.capitalizedName + (type.isRequired ? '*' : '')}</label>
                <Dropdown
                    options={timeUnits}
                    defaultOption={getArg(step, propName).value || timeUnits[0]}
                    onChange={timeUnit => {
                        setArg(step, propName, timeUnit);
                        onChange(step);
                    }}
                />
            </div>
        );
    }
}

TimeUnitPropertyInput.propTypes = {
    propName: PropTypes.string,
    step: PropTypes.any,
    onChange: PropTypes.func,
    type: PropTypes.any,
};

TimeUnitPropertyInput.dataTypes = ['java.util.concurrent.TimeUnit'];
