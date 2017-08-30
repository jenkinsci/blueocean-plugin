import React from 'react';
import SvgIcon from '../../SvgIcon';

const CommunicationChatBubble = (props) => (
  <SvgIcon {...props}>
    <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/>
  </SvgIcon>
);
CommunicationChatBubble.displayName = 'CommunicationChatBubble';
CommunicationChatBubble.muiName = 'SvgIcon';

export default CommunicationChatBubble;
