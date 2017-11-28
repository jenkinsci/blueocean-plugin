import React from 'react';
import SvgIcon from '../../SvgIcon';

const EditorTitle = (props) => (
  <SvgIcon {...props}>
    <path d="M5 4v3h5.5v12h3V7H19V4z"/>
  </SvgIcon>
);
EditorTitle.displayName = 'EditorTitle';
EditorTitle.muiName = 'SvgIcon';

export default EditorTitle;
