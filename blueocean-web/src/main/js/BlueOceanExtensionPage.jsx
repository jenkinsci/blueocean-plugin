import React from 'react';

export const BlueOceanExtensionPage = (renderFn) => class extends React.Component {
    render() {
        return <div ref={container => !!container && renderFn({container})}></div>
    }
}