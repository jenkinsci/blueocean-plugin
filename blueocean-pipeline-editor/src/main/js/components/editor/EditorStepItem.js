import React from 'react';
import { findDOMNode } from 'react-dom';
import { PropTypes } from 'react';
import { DragSource, DropTarget } from 'react-dnd';
import { Icon } from '@jenkins-cd/design-language';

import pipelineStore from '../../services/PipelineStore';
import { getArg } from '../../services/PipelineMetadataService';
import { ChildStepIcon } from "./ChildStepIcon";

const ItemType = 'EditorStepItem';

/*
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
*/

function dragSourceCollector(connect, monitor) {
    return {
        connectDragSource: connect.dragSource(),
        connectDragPreview: connect.dragPreview(),
        isDragging: monitor.isDragging(),
        isHovering: false,
    };
}

function dropTargetCollector(connect, monitor) {
    // const item = monitor.getItem() || {};

    return {
        connectDropTarget: connect.dropTarget(),
        isHovering: monitor.isOver(),
        isDroppable: monitor.canDrop(),
        //lastPosition: item && item.lastPosition,
    };
}

const dragSource = {
    beginDrag(props) {
        const id = props.step && props.step.id || -1;
        const dragSource = {
            id,
            sourceId: id,
            targetId: null,
            targetType: null,
            //lastPosition: null,
        };

        // workaround a bug in Chrome where 'dragend' would fire immediately after this 'dragstart' handler was called
        // occurs when container step 'drop targets' appear which push other steps down and change the drag handle position
        // see: https://stackoverflow.com/questions/14203734/dragend-dragenter-and-dragleave-firing-off-immediately-when-i-drag
        setTimeout(() => props.onDragStepBegin(dragSource), 5);
        return dragSource;
    },
    endDrag(props) {
        props.onDragStepEnd();
    }
};

const dragTarget = {
    hover(props, monitor, component) {
        /*
        const { lastPosition } = monitor.getItem();

        const below = calculateRelativeDragPosition(component, monitor.getClientOffset());
        const currentPosition = new ItemDragPosition(props.step.id, below);

        if (currentPosition.equals(lastPosition)) {
            return;
        }
        */

        const item = monitor.getItem();
        item.targetId = props.step.id;
        item.targetType = 'beforeItem';
        // item.lastPosition = currentPosition;
        props.onDragStepHover(item);
    },
    canDrop(props, monitor) {
        const item = monitor.getItem();
        const { stage, step, parent } = props;
        const ancestors = pipelineStore.findStepHierarchy(parent || step, stage.steps);
        const ancestorIds = ancestors.map(anc => anc.id);
        return ancestorIds.indexOf(item.id) === -1;
    },
    drop(props, monitor) {
        props.onDragStepDrop(monitor.getItem());
    }
};


@DragSource(ItemType, dragSource, dragSourceCollector)
@DropTarget(ItemType, dragTarget, dropTargetCollector)
class EditorStepItem extends React.Component {

    static propTypes = {
        stage: PropTypes.object,
        step: PropTypes.object,
        parent: PropTypes.object,
        parameters: PropTypes.array,
        errors: PropTypes.array,
        onDragStepBegin: PropTypes.func,
        onDragStepHover: PropTypes.func,
        onDragStepDrop: PropTypes.func,
        onDragStepEnd: PropTypes.func,
        // injected by react-dnd
        isHovering: PropTypes.bool,
        isDragging: PropTypes.bool,
        isDroppable: PropTypes.bool,
        connectDragSource: PropTypes.func,
        connectDragPreview: PropTypes.func,
        connectDropTarget: PropTypes.func,
    };

    static defaultProps = {
        onDragStepBegin: () => {},
        onDragStepHover: () => {},
        onDragStepDrop: () => {},
    };

    onDragHandleClick = (event) => {
        event.stopPropagation();
    };

    render() {
        const {
            step, parent, parameters, errors, isHovering, isDragging, isDroppable,
            connectDragSource, connectDragPreview, connectDropTarget,
        } = this.props;

        let dragClass = '';

        if (isDragging) {
            dragClass = 'is-dragging';
        } else if (isHovering && isDroppable) {
            dragClass = 'is-drop-allowed';
        } else if (isHovering && !isDroppable) {
            dragClass = 'is-drop-blocked';
        }

        return (connectDragPreview(connectDropTarget(
            <div className={`editor-step-content ${dragClass}`}>
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
                    <div className="editor-step-drag" onClick={this.onDragHandleClick}>
                        <Icon icon="EditorDragHandle" />
                    </div>
                )}
            </div>
        )));
    }
}

export { EditorStepItem };
