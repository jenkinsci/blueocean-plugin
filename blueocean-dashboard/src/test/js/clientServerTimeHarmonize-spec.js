import {TimeManager as timeManager} from '../../main/js/util/serverBrowserTimeHarmonize';
import {assert} from 'chai';
import moment from 'moment';

const TimeManager = new timeManager();
const runs = [{
  isRunning: true,
  startTime: "2017-01-26T10:28:34.755+0100",
  durationInMillis: 0,
}, {
  isRunning: true,
  startTime: "2017-01-26T12:24:42.557+0100",
  durationInMillis: 0,
}, {
  startTime: "2017-01-26T11:27:13.471Z",
  endTime: "2017-01-26T12:24:15.137+0100",
  durationInMillis: 6940382,
}];

describe("Logic to test the sync of time between server and client", () => {
  it('Client GMT-7 running job - pause state', () => {
    const skewMillis = -7118377;
    TimeManager.currentTime = () => moment(1485441519326);
    const {
      durationMillis,
      endTime,
      startTime,
    } = TimeManager.harmonizeTimes(runs[0], skewMillis);
    assert.equal(durationMillis, 11486194);
    assert.equal(startTime, "2017-01-26T11:27:13.132Z");
    assert.equal(endTime, null);
  }),
    it('Client GMT+9 running job - pause state', () => {
      const skewMillis = -7119267;
      TimeManager.currentTime = () => moment(1485441093121);
      const {
        durationMillis,
        endTime,
        startTime,
      } = TimeManager.harmonizeTimes(runs[1], skewMillis);
      assert.equal(durationMillis, 4091297);
      assert.equal(startTime, "2017-01-26T13:23:21.824Z");
      assert.equal(endTime, null);
    }),
    it('Client GMT+9 end job', () => {
      const skewMillis = -7118719;
      TimeManager.currentTime = () => moment(1485441093121);
      const {
        durationMillis,
        endTime,
        startTime,
      } = TimeManager.harmonizeTimes(runs[2], skewMillis);
      assert.equal(durationMillis, 6940382);
      assert.equal(endTime, "2017-01-26T13:22:53.856Z");
      assert.equal(startTime, "2017-01-26T13:25:52.190Z");
    })

});