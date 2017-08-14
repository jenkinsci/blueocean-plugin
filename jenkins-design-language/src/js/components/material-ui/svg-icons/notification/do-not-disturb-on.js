import React from 'react';
import SvgIcon from '../../SvgIcon';

const NotificationDoNotDisturbOn = (props) => (
  <SvgIcon {...props}>
    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm5 11H7v-2h10v2z"/>
  </SvgIcon>
);
NotificationDoNotDisturbOn.displayName = 'NotificationDoNotDisturbOn';
NotificationDoNotDisturbOn.muiName = 'SvgIcon';

export default NotificationDoNotDisturbOn;
