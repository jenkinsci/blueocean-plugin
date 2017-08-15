import React from 'react';
import SvgIcon from '../../SvgIcon';

const ImagePhoto = (props) => (
  <SvgIcon {...props}>
    <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
  </SvgIcon>
);
ImagePhoto.displayName = 'ImagePhoto';
ImagePhoto.muiName = 'SvgIcon';

export default ImagePhoto;
