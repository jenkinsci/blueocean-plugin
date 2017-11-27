import React from 'react';
import SvgIcon from '../../SvgIcon';

const DeviceNetworkCell = (props) => (
  <SvgIcon {...props}>
    <path fillOpacity=".3" d="M2 22h20V2z"/><path d="M17 7L2 22h15z"/>
  </SvgIcon>
);
DeviceNetworkCell.displayName = 'DeviceNetworkCell';
DeviceNetworkCell.muiName = 'SvgIcon';

export default DeviceNetworkCell;
