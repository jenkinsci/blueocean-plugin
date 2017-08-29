import React from 'react';
import SvgIcon from '../../SvgIcon';

const NavigationArrowBack = (props) => (
  <SvgIcon {...props}>
    <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
  </SvgIcon>
);
NavigationArrowBack.displayName = 'NavigationArrowBack';
NavigationArrowBack.muiName = 'SvgIcon';

export default NavigationArrowBack;
