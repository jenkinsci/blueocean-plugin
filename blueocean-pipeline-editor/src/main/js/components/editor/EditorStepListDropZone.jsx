import React from 'react';
import { PropTypes } from 'react';
import { DropTarget } from 'react-dnd';

import { ChildStepIcon} from "./ChildStepIcon";


const ItemType = 'EditorStepItem';

function dropTargetCollector(connect, monitor) {
    const item = monitor.getItem() || {};

    return {
        connectDropTarget: connect.dropTarget(),
        isHovering: monitor.isOver(),
        lastPosition: item && item.lastPosition,
    };
}

const dropTarget = {
    hover(props, monitor) {
        const item = monitor.getItem();
        item.targetId = props.parent.id;
        item.targetType = 'childItem';
        props.onDragStepHover(item);
    },
    drop(props, monitor) {
        const item = monitor.getItem();
        props.onDragStepDrop(item);
    }
    // TODO: impl canDrop to block dragging a parent into a descendant
};


@DropTarget(ItemType, dropTarget, dropTargetCollector)
class EditorStepListDropZone extends React.Component {

    static propTypes = {
        parent: PropTypes.object,
        onDragStepHover: PropTypes.func,
        onDragStepDrop: PropTypes.func,
        // injected by react-dnd
        isHovering: PropTypes.bool,
        connectDropTarget: PropTypes.func,
    };

    static defaultProps = {
        onDragStepHover: () => {},
        onDragStepDrop: () => {},
    };

    render() {
        const { parent, isHovering, connectDropTarget } = this.props;
        const isStage = !!parent.steps;
        const hoverClass = isHovering && 'is-dragged-over';

        return (connectDropTarget(
            <div className={`editor-step-list-drop-zone ${hoverClass}`}>
                <ChildStepIcon />
                <div className="drop-zone-label">
                    { isStage && <span>move to bottom</span> }
                    { !isStage && <span>move to <strong>{parent.name}</strong></span> }
                </div>
            </div>
        ));
    }
}

export { EditorStepListDropZone };
