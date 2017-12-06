import React from 'react';
import { PropTypes } from 'react';
import { DropTarget } from 'react-dnd';

import pipelineStore from '../../services/PipelineStore';
import { ChildStepIcon} from "./ChildStepIcon";


const ItemType = 'EditorStepItem';

function dropTargetCollector(connect, monitor) {
    // const item = monitor.getItem() || {};

    return {
        connectDropTarget: connect.dropTarget(),
        isHovering: monitor.isOver(),
        isDroppable: monitor.canDrop(),
        //lastPosition: item && item.lastPosition,
    };
}

const dropTarget = {
    hover(props, monitor) {
        const item = monitor.getItem();
        item.targetId = props.parent.id;
        item.targetType = 'childItem';
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
        const item = monitor.getItem();
        props.onDragStepDrop(item);
    }
};


@DropTarget(ItemType, dropTarget, dropTargetCollector)
class EditorStepListDropZone extends React.Component {

    static propTypes = {
        stage: PropTypes.object,
        parent: PropTypes.object,
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
        const { parent, isHovering, isDroppable, connectDropTarget } = this.props;
        const isStage = !!parent.steps;
        const dragClass = isHovering && (isDroppable && 'is-drop-allowed' || 'is-drop-blocked');

        return (connectDropTarget(
            <div className={`editor-step-list-drop-zone ${dragClass}`}>
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
