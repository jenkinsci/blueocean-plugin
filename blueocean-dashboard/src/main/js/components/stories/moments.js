import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { TimeDuration, ReadableDate } from '@jenkins-cd/design-language';

storiesOf('TimeDuration', module)
  .add('short duration - locale de', () => (
    <TimeDuration
      millis={50000}
      locale="de"
      hintFormat="M [mos], d [Tage], h[Std.], m[m], s[s]"
      liveFormat="m[ Minuten] s[ Sekunden]"
    />
  ))
  .add('long duration, all date parts - locale de', () => (
    <TimeDuration
      updatePeriod={3000}
      millis={3.5 * 1000 * 60 * 60 * 24 * 7 * 4 * 6 + 1001 * 60 * 60 * 4.75}
      locale="de"
      hintFormat="M [Monate], d [Tage], h[h], m[m], s[s]"
      liveFormat="m[ Minuten] s[ Sekunden]"
    />
  ))
;

storiesOf('ReadableDate', module)
  .add('standard distant', () => (
    <ReadableDate
      locale="de"
      date="2015-05-24T08:57:06.406+0000"
      longFormat="MMM DD YYYY h:mma Z"
      shortFormat="MMM DD h:mma Z"
    />
  ))
;
