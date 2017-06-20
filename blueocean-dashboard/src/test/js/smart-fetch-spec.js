import { assert } from 'chai';
import nock from 'nock';
import { fetch, paginate } from '../../main/js/util/smart-fetch';
const debug = require('debug')('smart-fetch-test:debug');
import { TestUtils, Fetch, FetchFunctions } from '@jenkins-cd/blueocean-core-js';

xdescribe("smart-fetch", () => {
  beforeEach(() => {
      TestUtils.patchFetchNoJWT();
  })
  afterEach(() => {
      nock.cleanAll()
  });

  it("Callbacks work", (done) => {
      var mockSrv = nock('http://example.com')
      mockSrv.get('/1').reply(200, function (uri, requestBody) {
        return '{}';
      });
      var rval = fetch('http://example.com/1', (data) => {
          if (data.$pending) { return; }
          assert(data, "no val!");
          done();
      });
      assert(rval.then, "not thenable");
  });

  it("Promises work", (done) => {
      var mockSrv = nock('http://example.com')
      mockSrv.get('/1').reply(200, function (uri, requestBody) {
        return '{}';
      });

      var rval = fetch('http://example.com/1');
      assert(rval.then, "no promise returned!");
      rval.then((data) => {
          if (data.$pending) { return; }
          assert(data, "no val!");
          done();
      });
  });

  it("Sends fetch status notifications", done => {
      var mockSrv = nock('http://example.com')
      mockSrv.get('/1').reply(200, function (uri, requestBody) {
        return '{"word":"up"}';
      });
      var rval = fetch('http://example.com/1', data => {
          if(data.$success) {
              assert(data.word && data.word === 'up');
              done();
              return;
          }
          console.log("blad");
          // will get here first
          assert(data.$pending);
      });
  });

  it("Fetch multiple works", (done) => {
      var mockSrv = nock('http://example.com');

      var responseStream = new require('stream').Readable();
      var fetchCount = 0;
      var callCount = 0;
      mockSrv.get('/1').reply(200, function (uri, requestBody) {
          fetchCount++;
          return responseStream;
      });

      mockSrv.get('/2').reply(200, function (uri, requestBody) {
        return "reply to second request";
      });

      fetch('http://example.com/1', data => data.$success && callCount++);
      fetch('http://example.com/1', data => data.$success && callCount++);
      fetch('http://example.com/1', data => {
          if (data.$pending) { return; }
          if (data.$success) {
              assert(fetchCount == 1, "wrong fetch count: " + fetchCount);
              assert(callCount == 2, "wrong call count: " + callCount); // didn't increment it here
              assert(data.mydata == 'ok', "wrong response: " + data);
              done();
          }
      });

      responseStream.push('{"mydata":"ok"}\n');
      responseStream.push(null);
  });

  it("Pagination works", (done) => {
      var calls = 0;
      var mockSrv = nock('http://example.com')
      mockSrv.get('/?start=0&limit=3').reply(200, function (uri, requestBody) { // fetches an extra 1 to check for more
          calls++;
          debug('smart-fetch-spec.pagination 1');
        return '[{"name":"thing1"},{"name":"thing2"},{"name":"thing3"}]';
      });
      mockSrv.get('/?start=2&limit=3').reply(200, function (uri, requestBody) {
          calls++;
          debug('smart-fetch-spec.pagination 2');
        return '[{"name":"thing3"},{"name":"thing4"},{"name":"thing5"}]';
      });
      mockSrv.get('/?start=4&limit=3').reply(200, function (uri, requestBody) {
          calls++;
          debug('smart-fetch-spec.pagination 3');
        return '[{"name":"thing5"},{"name":"thing6"}]';
      });

      var callbacks = [];
      paginate({
        urlProvider: (start, limit) => `http://example.com/?start=${start}&limit=${limit}`,
        startIndex: 0,
        pageSize: 2,
      })
      .then(data => {
        callbacks.push(data);
        if (data.$pending) return;
        if(callbacks.length == 6) { // should have 3 pending, 3 data
            assert(calls == 3, "incorrect number of fetches");
            assert(!data.$pager.hasMore, "should not have more data")
            assert(data.length == 6, 'should have 6 items');
            assert(data[0].name === 'thing1', 'wrong name');
            assert(data[1].name === 'thing2', 'wrong name');
            assert(data[2].name === 'thing3', 'wrong name');
            assert(data[3].name === 'thing4', 'wrong name');
            assert(data[4].name === 'thing5', 'wrong name');
            assert(data[5].name === 'thing6', 'wrong name');
            done();
            return;
        }
        data.$pager.fetchMore();
    });
  });
});
