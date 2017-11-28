import React from 'react';
import SvgIcon from '../../SvgIcon';

const AvPause = (props) => (
  <SvgIcon {...props}>
    <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>
  </SvgIcon>
);
AvPause.displayName = 'AvPause';
AvPause.muiName = 'SvgIcon';

export default AvPause;
