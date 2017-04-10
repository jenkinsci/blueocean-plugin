import React from 'react';
import { storiesOf } from '@kadira/storybook';

import { Icon, shapes } from '@jenkins-cd/react-material-icons';

const custom = `<path
     d="m 19,12.473273 -4,0 L 15,3 9,3 l 0,9.473273 -4,0 7,11.052151 7,-11.052151 z"
     id="path3541" />
  <path
     d="m 5,6.4799998 0,2.0000001 14,0 0,-2.0000001 -14,0 z"
     id="path3543" />
  <path
     d="m 5.036611,2.8603396 0,2.0000001 13.999999,0 0,-2.0000001 -13.999999,0 z"
     id="path3543-9" />
`;

storiesOf('Blue Icon', module)
    .add('icon link', () => (
    <a
      title="Display the log in new window"
    >
        <Icon
          icon="link"// Icon in the field transformation
          style={{ fill: 'red' }} // Styles prop for icon (svg)
        />
        Link
    </a>
    ))
    .add('icon custom', () => (
        <Icon
          custom={custom}
          size={500}
          style={{ fill: 'red' }} // Styles prop for icon (svg)
        />
    ))
    .add('all', () => (
    <div>
        {
            Object.keys(shapes).map((shape, index) => <div
              key={index}
              style={
                  {
                      display: 'inline',
                      float: 'left',
                      padding: '5px',
                  }
              }
            >
                <Icon icon={shape} />
                <div>{shape}</div>
            </div>)
        }
    </div>
));
