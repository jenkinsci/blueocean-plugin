const env = require('./env');

const consoleLogger = {

  log: function (args) {
    this._output('log', args);
  },

  warn: function (args) {
    this._output('warn', args);
  },

  error: function (args) {
    this._output('error', args);
  },

  _output: function (type, args) {
    if (window.console && window.console[type]) {
      window.console[type].apply(window.console, Array.prototype.slice.call(args));
    }
  }

};

module.exports = {

  _verbose: false,
  _logInProduction: false,

  logger: consoleLogger,

  init: function (logger, options) {
    this.logger = logger;
    if (options && options.verbose) {
      this._verbose = options.verbose;
    }
  },

  setVerbose: function (bool) {
    this._verbose = bool;
  },

  logInProduction: function (bool) {
    this._logInProduction = bool;
  },

  shouldLog: function () {
    return this._logInProduction || (env.debug && env.isBrowser);
  },

  log: function () {
    if (this._verbose && this.shouldLog()) {
      this.logger.log(arguments);
    }
  },

  warn: function () {
    if (this.shouldLog()) {
      this.logger.warn(arguments);
    }
  },

  error: function () {
    if (this.shouldLog()) {
      this.logger.error(arguments);
    }
  }

};
