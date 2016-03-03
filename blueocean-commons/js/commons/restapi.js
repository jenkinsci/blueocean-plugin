import logger from './logger';
import env from './env';

const calledRoutes = {};

const api = {

  app: {
    settings: {}
  },

  init: function (options) {
    this.host = options.host;
    this.app = options.app;
    this.options = options;
  },

  applyReplace: function (route) {

// Activate again as soon router comes into the mix
    /*let routeState = router.getState();
    for (let n in routeState.params) {
      if (routeState.params.hasOwnProperty(n)) {
        route = route.replace(':' + n, routeState.params[n]);
      }
    }*/
    route = route.replace(/\[\/*:\w*\]/g, '');
    route = route.replace(/\[|\]/g, '');

    return route;
  },

  replaceParams: function (route, callback) {
    const self = this;

    function finished(err, r) {
      if (err) {
        logger.warn(err);
        return callback(err);
      }

      // warning
      const ignoreWarningFor = ['/usercontext'];
      if (ignoreWarningFor.indexOf(r) < 0) {
        const called = calledRoutes[r];
        const now = new Date();

        if (called && now - called < 1000) {
          logger.warn(`restapi: route "${r}" was called multiple times in one second.`);
        }
        calledRoutes[r] = now;
      }

      if (env.browser.msie) { // http://stackoverflow.com/questions/3984961/prevent-xmlhttpreq-s-browser-caching
        let sep = '?';
        if (r.indexOf('?') > 0) {
          sep = '&';
        }
        r += `${sep} timestamp=${(new Date()).getTime()}`;
      }
      callback(err, r);
    }

    if (route.indexOf('/:') < 0) {
      if (env.debug && this.options.latency) {
        setTimeout(() => {
          finished(null, route);
        }, this.options.latency);
      } else {
        finished(null, route);
      }
      return;
    }

    let retries = 5,
        timeout = 200;

    function tryReplace() {
      if (route.indexOf('/:') < 0){
        return finished(null, route);
      }

      if (retries) {
        route = self.applyReplace(route);
        if (route.indexOf('/:') < 0) {
          return finished(null, route);
        }

        retries--;
        timeout = timeout * 2;
        setTimeout(tryReplace, timeout);

      } else {
        finished(`could not resolve params in api route:${route}`);
      }
    }

    if (env.debug && this.options.latency) {
      setTimeout( () => {
        tryReplace();
      }, this.options.latency);
    } else {
      tryReplace();
    }

  },

  getJSON: function (route, partial, callback) {
    if (typeof partial === 'function') {
      callback = partial;
      partial = null;
    }


    function get(url) {
      const xhr = new XMLHttpRequest();
      xhr.open('GET', url, true);

      xhr.onreadystatechange = function () {
        let responseData = xhr.responseText;
        if (xhr.readyState !== 4) {
          return;
        }

        if (responseData) {
          try {
            responseData = JSON.parse(responseData);
          } catch(e) {
            // might be just a string --> authentication error
          }
        }

        if (xhr.status !== 200) {
          callback(new Error(`Status code ${xhr.status} ${url}`),
           responseData.response ? responseData.response : responseData);
        }

        if (!responseData.response) {
          callback(null, responseData);
        }

        if (partial) {
          callback(null, responseData.response[partial]);
          }
        callback(null, responseData.response);
      };

      xhr.send();
    }

    this.replaceParams(route, (err, url) => {
      if (err) {
        return callback(err);
      }
      get(url);
    });
  },

  postOrPutJSON: function (method, route, data, partial, callback) {
    if (typeof partial === 'function') {
      callback = partial;
      partial = null;
    }

    function post(url) {
      const xhr = new XMLHttpRequest();
      xhr.open(method, url, true);
      xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');

      xhr.onreadystatechange = function () {
        let responseData = xhr.responseText;
        if (xhr.readyState !== 4) {
          return;
        }

        if (responseData) {
          try {
            responseData = JSON.parse(responseData);
          } catch(e) {
            // might be just a string --> authentication error
          }
        }

        if (xhr.status !== 200) {
          callback(new Error(`Status code ${xhr.status} ${url}`),
            responseData.response ? responseData.response : responseData);
        }

        if (!responseData.response) {
          callback(null, responseData);
        }

        if (partial) {
          callback(null, responseData.response[partial]);
        }
        callback(null, responseData.response);
      };

      xhr.send(JSON.stringify(data));
    }

    this.replaceParams(route, (err, url) => {
      if (err) {return callback(err); }
      post(url);
    });
  },

  postJSON: function (route, data, partial, callback) {
    this.postOrPutJSON('POST', route, data, partial, callback);
  },

  putJSON: function (route, data, partial, callback) {
    this.postOrPutJSON('PUT', route, data, partial, callback);
  },

  delete: function (route, callback) {
    function del(url) {
      const xhr = new XMLHttpRequest();
      xhr.open('DELETE', url, true);

      xhr.onreadystatechange = function () {
        let responseData = xhr.responseText;
        if (xhr.readyState !== 4) {
          return;
        }

        if (responseData) {
          responseData = JSON.parse(responseData);
        }

        if (xhr.status !== 200) {
          callback(new Error(`Status code ${xhr.status} ${url}`),
           responseData.response ? responseData.response : responseData);
        }

        callback(null, responseData.response);
      };

      xhr.send();
    }

    this.replaceParams(route, (err, url) => {
      if (err) {return callback(err); }
      del(url);
    });
  },

  /**
   * Custom XHR handling for upload FormData, since superagent doesn't
   * recognize it and screws up the Content-Type.
   *
   * @param {string} route
   * @param {object|FormData} formData
   * @param {function} [callback]   callback(error, responseData)
   */
   postOrPutFormData: function (method, route, formData, callback, progress) {
    function post(url) {
      const xhr = new XMLHttpRequest();
      if (xhr.upload) {
        xhr.upload.onprogress = function (oEvent) {
          if (oEvent.lengthComputable) {
            const percentComplete = oEvent.loaded / oEvent.total * 100;
            logger.log(`${method}:${url} - percentComplete: ${percentComplete}`);
            if (progress) {
              progress(percentComplete);
            }
          }
        };
      }

      xhr.open(method, url, true);
      if (formData && formData.indexOf('image') >= 0) {
        xhr.setRequestHeader('Content-Type', 'text/plain;charset=UTF-8');
      }

      xhr.onreadystatechange = function () {
        let responseData = xhr.responseText;
        if (xhr.readyState !== 4) {
          return;
        }

        if (responseData) {
          responseData = JSON.parse(responseData);
        }

        if (xhr.status !== 200) {
          callback(new Error(`Status code ${xhr.status} ${url}`),
           responseData.response ? responseData.response : responseData);
        }

        callback(null, responseData.response);
      };

      xhr.send(formData);
    }

    this.replaceParams(route, (err, url) => {
      if (err) {
        return callback(err);
      }
      post(url);
    });
  },

  postFormData: function (route, formData, callback, progress) {
    this.postOrPutFormData('POST', route, formData, callback, progress);
  },

  putFormData: function (route, formData, callback, progress) {
    this.postOrPutFormData('PUT', route, formData, callback, progress);
  }

};

module.exports = api;
