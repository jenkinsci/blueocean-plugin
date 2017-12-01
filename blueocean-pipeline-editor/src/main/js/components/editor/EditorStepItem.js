import React from 'react';
import { findDOMNode } from 'react-dom';
import { PropTypes } from 'react';
import { DragSource, DropTarget } from 'react-dnd';
import { Icon } from '@jenkins-cd/design-language';
import { getArg } from '../../services/PipelineMetadataService';

const ItemType = 'EditorStepItem';

class ItemDragPosition {
    id = null;
    below = false;

    constructor(id, below) {
        this.id = id;
        this.below = below;
    }

    equals(position) {
        return position && this.id === position.id && this.below === position.below;
    }
}

function calculateRelativeDragPosition(component, clientOffset) {
    const hoverBoundingRect = findDOMNode(component).getBoundingClientRect();
    const hoverMiddleY = (hoverBoundingRect.bottom - hoverBoundingRect.top) / 2;
    const hoverClientY = clientOffset.y - hoverBoundingRect.top;
    return hoverClientY >= hoverMiddleY;
}

function dragSourceCollector(connect, monitor) {
    return {
        connectDragSource: connect.dragSource(),
        connectDragPreview: connect.dragPreview(),
        isDragging: monitor.isDragging(),
        isHovering: false,
    };
}

function dropTargetCollector(connect, monitor) {
    const item = monitor.getItem() || {};

    return {
        connectDropTarget: connect.dropTarget(),
        isHovering: monitor.isOver(),
        hoverBelow: item && item.lastPosition && item.lastPosition.below,
        lastPosition: item && item.lastPosition,
    };
}

const cardSource = {
    beginDrag(props) {
        const id = props.step && props.step.id || -1;

        return {
            id,
            lastPosition: null,
        };
    },
};

const cardTarget = {
    hover(props, monitor, component) {
        const { id, lastPosition } = monitor.getItem();

        const below = calculateRelativeDragPosition(component, monitor.getClientOffset());
        const currentPosition = new ItemDragPosition(props.step.id, below);

        if (currentPosition.equals(lastPosition)) {
            return;
        }

        props.hoverOverStep(id, currentPosition);
        monitor.getItem().lastPosition = currentPosition;
    },
    drop(props, monitor) {
        const { id, lastPosition } = monitor.getItem();
        props.dropOnStep(id, lastPosition);
    }
};

function ChildStepIcon() {
    return (<div className="editor-step-child-icon">
        <svg fill="#000000" height="16" viewBox="0 0 24 24" width="16" xmlns="http://www.w3.org/2000/svg">
            <path d="M0 0h24v24H0V0z" fill="none"/>
            <path d="M19 15l-6 6-1.42-1.42L15.17 16H4V4h2v10h9.17l-3.59-3.58L13 9l6 6z"/>
        </svg>
    </div>);
}


@DragSource(ItemType, cardSource, dragSourceCollector)
@DropTarget(ItemType, cardTarget, dropTargetCollector)
class EditorStepItem extends React.Component {

    static propTypes = {
        step: PropTypes.object,
        parent: PropTypes.object,
        parameters: PropTypes.array,
        errors: PropTypes.array,

        connectDragSource: PropTypes.func,
        connectDragPreview: PropTypes.func,
        connectDropTarget: PropTypes.func,
        hoverOverStep: PropTypes.func,
        dropOnStep: PropTypes.func,
    };

    render() {
        const {
            step, parent, parameters, errors,
            connectDragSource, connectDragPreview, connectDropTarget,
        } = this.props;

        return (connectDragPreview(connectDropTarget(
            <div className="editor-step-content">
                {parent && <ChildStepIcon/>}
                <div className="editor-step-title">
                    <span className="editor-step-label">{step.label}</span>
                    {!errors &&
                        <span className="editor-step-summary">
                        {parameters && parameters.filter(p => p.isRequired).map(p =>
                            <span>{getArg(step, p.name).value} </span>
                        )}
                        </span>
                    }
                    {errors &&
                        <span className="editor-step-errors">
                        {errors.map(err =>
                            <div>{err.error ? err.error : err}</div>
                        )}
                        </span>
                    }
                </div>
                {connectDragSource(
                    <div className="editor-step-drag">
                        <Icon icon="EditorDragHandle" />
                    </div>
                )}
            </div>
        )));
    }
}

export { EditorStepItem };
