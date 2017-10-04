import React from 'react';
import SvgIcon from '../../SvgIcon';

const EditorSpaceBar = (props) => (
  <SvgIcon {...props}>
    <path d="M18 9v4H6V9H4v6h16V9z"/>
  </SvgIcon>
);
EditorSpaceBar.displayName = 'EditorSpaceBar';
EditorSpaceBar.muiName = 'SvgIcon';

export default EditorSpaceBar;
