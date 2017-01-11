import React, { PropTypes } from 'react';
import { storiesOf } from '@kadira/storybook';
import { ErrorMessage } from '../components/ErrorMessage';

storiesOf('ErrorMessage', module)
    .add('sizes', () => <Sizes />)
;

const style = {
    padding: 10,
};

// Sizes

// show several ErrorMessages w/ spacing
function ErrorMessageGroup(props) {
    const count = props.count || 3;
    const array = new Array(count);

    return (
        <div>
            {React.Children.map(array, (item, index) => (
                <ErrorMessage>Error Message #{index}</ErrorMessage>
            ))}
        </div>
    );
}

ErrorMessageGroup.propTypes = {
    count: PropTypes.number,
};

function Sizes() {
    return (
        <div>
            <div style={style}>
                <p>Using no layout</p>
                <ErrorMessageGroup />
            </div>
            <div className="layout-small" style={style}>
                <p>Using layout-small</p>
                <ErrorMessageGroup />
            </div>
            <div className="layout-medium" style={style}>
                <p>Using layout-medium</p>
                <ErrorMessageGroup />
            </div>
            <div className="layout-large" style={style}>
                <p>Using layout-large</p>
                <ErrorMessageGroup />
            </div>
        </div>
    );
}
