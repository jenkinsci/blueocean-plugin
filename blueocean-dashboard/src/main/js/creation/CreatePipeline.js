/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';

function DialogPlaceholder(props) {
    function closeHandler() {
        if (props.onClose) {
            props.onClose();
        }
    }

    return (
        <div style={{ position: 'fixed', zIndex: 50, top: 50, left: 100, right: 100, bottom: 50, background: '#fff' }}>
            <div style={{ position: 'absolute', zIndex: 100, top: 0, left: 0, right: 0, bottom: 0 }}>
                {props.children}
            </div>
            <a style={{ position: 'absolute', zIndex: 100, top: 10, right: 10, cursor: 'pointer' }} onClick={closeHandler}>CLOSE</a>
        </div>
    );
}

DialogPlaceholder.propTypes = {
    children: PropTypes.node,
    onClose: PropTypes.function,
};

export class CreatePipeline extends React.Component {
    _exit() {
        this.context.router.goBack();
    }

    render() {
        return (
            <DialogPlaceholder onClose={() => this._exit()}>
                Create Pipeline
            </DialogPlaceholder>
        );
    }
}

CreatePipeline.contextTypes = {
    router: PropTypes.object,
};
