import React from 'react';
import SvgIcon from '../../SvgIcon';

const AvVolumeMute = (props) => (
  <SvgIcon {...props}>
    <path d="M7 9v6h4l5 5V4l-5 5H7z"/>
  </SvgIcon>
);
AvVolumeMute.displayName = 'AvVolumeMute';
AvVolumeMute.muiName = 'SvgIcon';

export default AvVolumeMute;
