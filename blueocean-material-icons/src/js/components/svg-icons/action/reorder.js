import React from 'react';
import SvgIcon from '../../SvgIcon';

const ActionReorder = (props) => (
  <SvgIcon {...props}>
    <path d="M3 15h18v-2H3v2zm0 4h18v-2H3v2zm0-8h18V9H3v2zm0-6v2h18V5H3z"/>
  </SvgIcon>
);
ActionReorder.displayName = 'ActionReorder';
ActionReorder.muiName = 'SvgIcon';

export default ActionReorder;
