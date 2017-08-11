import React from 'react';
import SvgIcon from '../../SvgIcon';

const ActionHome = (props) => (
  <SvgIcon {...props}>
    <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
  </SvgIcon>
);
ActionHome.displayName = 'ActionHome';
ActionHome.muiName = 'SvgIcon';

export default ActionHome;
