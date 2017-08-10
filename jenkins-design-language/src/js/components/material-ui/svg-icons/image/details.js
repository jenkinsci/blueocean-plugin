import React from 'react';
import SvgIcon from '../../SvgIcon';

const ImageDetails = (props) => (
  <SvgIcon {...props}>
    <path d="M3 4l9 16 9-16H3zm3.38 2h11.25L12 16 6.38 6z"/>
  </SvgIcon>
);
ImageDetails.displayName = 'ImageDetails';
ImageDetails.muiName = 'SvgIcon';

export default ImageDetails;
