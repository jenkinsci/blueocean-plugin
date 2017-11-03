import React from 'react';
import SvgIcon from '../../SvgIcon';

const EditorDragHandle = (props) => (
  <SvgIcon {...props}>
    <path d="M20 9H4v2h16V9zM4 15h16v-2H4v2z"/>
  </SvgIcon>
);
EditorDragHandle.displayName = 'EditorDragHandle';
EditorDragHandle.muiName = 'SvgIcon';

export default EditorDragHandle;
