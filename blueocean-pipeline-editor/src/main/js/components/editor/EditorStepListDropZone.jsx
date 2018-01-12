import React from 'react';
import { PropTypes } from 'react';
import { DropTarget } from 'react-dnd';


const ItemType = 'EditorStepItem';

function dropTargetCollector(connect, monitor) {
    return {
        connectDropTarget: connect.dropTarget(),
        isHovering: monitor.isOver(),
        isDroppable: monitor.canDrop(),
    };
}

const dropTarget = {
    hover(props, monitor) {
        const item = monitor.getItem();
        item.targetId = props.step.id;
        item.targetType = props.position;
        props.onDragStepHover(item);
    },
    drop(props, monitor) {
        const item = monitor.getItem();
        props.onDragStepDrop(item);
    }
};

function positionToClass(dragPosition) {
    return dragPosition ? dragPosition.toLowerCase().replace('_', '-') : '';
}


@DropTarget(ItemType, dropTarget, dropTargetCollector)
class EditorStepListDropZone extends React.Component {

    static propTypes = {
        stage: PropTypes.object,
        step: PropTypes.object,
        position: PropTypes.string,
        onDragStepHover: PropTypes.func,
        onDragStepDrop: PropTypes.func,
        // injected by react-dnd
        isHovering: PropTypes.bool,
        isDroppable: PropTypes.bool,
        connectDropTarget: PropTypes.func,
    };

    static defaultProps = {
        onDragStepHover: () => {},
        onDragStepDrop: () => {},
    };

    render() {
        const { position, isHovering, isDroppable, connectDropTarget } = this.props;
        let dragClass = isHovering && (isDroppable && 'is-drop-allowed' || 'is-drop-blocked') || '';
        dragClass += ' ' + positionToClass(position);

        return (connectDropTarget(
            <div className={`editor-step-list-drop-zone ${dragClass}`}/>
        ));
    }
}

export { EditorStepListDropZone };
