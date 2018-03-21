import React from 'react';
import { findDOMNode } from 'react-dom';
import { PropTypes } from 'react';
import { DragSource } from 'react-dnd';
import { Icon } from '@jenkins-cd/design-language';

import { getArg } from '../../services/PipelineMetadataService';
import { EditorStepListDropZone } from './EditorStepListDropZone';
import { DragPosition } from './DragPosition';

const ItemType = 'EditorStepItem';

function dragSourceCollector(connect, monitor) {
    return {
        connectDragSource: connect.dragSource(),
        connectDragPreview: connect.dragPreview(),
        isDragging: monitor.isDragging(),
        isHovering: false,
    };
}

const dragSource = {
    beginDrag(props) {
        const id = (props.step && props.step.id) || -1;
        const dragSource = {
            id,
            sourceId: id,
            targetId: null,
            targetType: null,
        };

        // workaround a bug in Chrome where 'dragend' would fire immediately after this 'dragstart' handler was called
        // occurs when container step 'drop targets' appear which push other steps down and change the drag handle position
        // see: https://stackoverflow.com/questions/14203734/dragend-dragenter-and-dragleave-firing-off-immediately-when-i-drag
        setTimeout(() => props.onDragStepBegin(dragSource), 5);
        return dragSource;
    },
    endDrag(props) {
        props.onDragStepEnd();
    },
};

@DragSource(ItemType, dragSource, dragSourceCollector)
class EditorStepItem extends React.Component {
    static propTypes = {
        stage: PropTypes.object,
        step: PropTypes.object,
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
    };

    static defaultProps = {
        onDragStepBegin: () => {},
        onDragStepHover: () => {},
        onDragStepDrop: () => {},
    };

    onDragHandleClick = event => {
        event.stopPropagation();
    };

    render() {
        const { stage, step, parameters, errors, isHovering, isDragging, isDroppable, connectDragSource, connectDragPreview } = this.props;

        let dragClass = '';

        if (isDragging) {
            dragClass = 'is-dragging';
        } else if (isHovering && isDroppable) {
            dragClass = 'is-drop-allowed';
        } else if (isHovering && !isDroppable) {
            dragClass = 'is-drop-blocked';
        }

        const topDragPosition = DragPosition.BEFORE_ITEM;
        const botDragPosition = step.isContainer ? DragPosition.FIRST_CHILD : DragPosition.AFTER_ITEM;

        return connectDragPreview(
            <div className={`editor-step-content ${dragClass}`}>
                <div className="editor-step-title">
                    <span className="editor-step-label">{step.label}</span>
                    {!errors && (
                        <span className="editor-step-summary">
                            {parameters && parameters.filter(p => p.isRequired).map(p => <span>{getArg(step, p.name).value} </span>)}
                        </span>
                    )}
                    {errors && <span className="editor-step-errors">{errors.map(err => <div>{err.error ? err.error : err}</div>)}</span>}
                </div>
                <EditorStepListDropZone
                    stage={stage}
                    step={step}
                    position={topDragPosition}
                    onDragStepHover={this.props.onDragStepHover}
                    onDragStepDrop={this.props.onDragStepDrop}
                />
                <EditorStepListDropZone
                    stage={stage}
                    step={step}
                    position={botDragPosition}
                    onDragStepHover={this.props.onDragStepHover}
                    onDragStepDrop={this.props.onDragStepDrop}
                />
                {connectDragSource(
                    <div className="editor-step-drag" onClick={this.onDragHandleClick}>
                        <Icon icon="EditorDragHandle" />
                    </div>
                )}
            </div>
        );
    }
}

export { EditorStepItem };
