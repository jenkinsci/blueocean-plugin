import React from 'react';
import SvgIcon from '../../SvgIcon';

const NavigationArrowDropDown = (props) => (
  <SvgIcon {...props}>
    <path d="M7 10l5 5 5-5z"/>
  </SvgIcon>
);
NavigationArrowDropDown.displayName = 'NavigationArrowDropDown';
NavigationArrowDropDown.muiName = 'SvgIcon';

export default NavigationArrowDropDown;
