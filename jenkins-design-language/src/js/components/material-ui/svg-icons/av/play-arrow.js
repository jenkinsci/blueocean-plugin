import React from 'react';
import SvgIcon from '../../SvgIcon';

const AvPlayArrow = (props) => (
  <SvgIcon {...props}>
    <path d="M8 5v14l11-7z"/>
  </SvgIcon>
);
AvPlayArrow.displayName = 'AvPlayArrow';
AvPlayArrow.muiName = 'SvgIcon';

export default AvPlayArrow;
