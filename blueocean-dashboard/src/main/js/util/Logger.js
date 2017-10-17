/**
 * A logger that provides links back to the actual log message source in chrome debug tools
 * as well as providing stack traces automatically at DEBUG level
 *
 * and is easy to configure. It supports node's process.env and browser's localStorage
 * and even query params
 */

/**
 * A logging class that provides methods which correctly link to the script location
 * where logging happens
 */
export class Logger {
    /**
     * Basic logging levels
     */
    static Level = {
        ERROR: 0,
        WARN: 10,
        INFO: 20,
        LOG: 30,
        DEBUG: 40,
        TRACE: 50,
    };

    /**
     * List of any specified logging level rules
     * @type Array
     */
    static logLevels = [];

    /**
     * Creates a new Logger
     *
     * @argument {array<class|string>} channel log channel
     */
    constructor(...channel) {
        this._channel = channel
            .map(part => part.toString())
            .reduce((arr, val) => arr.concat(val.split('.')), [])
            .join('.');
        this.setLogLevel(this.getLevel());
    }

    setLogLevel(level) {
        this._logLevel = level;
        for (const l of Object.keys(Logger.Level)) {
            this.createLogger(l);
        }
    }

    createLogger(levelName) {
        const functionName = levelName.toLowerCase();
        if (this.isEnabled(Logger.Level[levelName])) {
            // If this channel is debugging enabled, use console.warn so we also get a stack trace, at least on chrome
            let fn = Logger.Level[levelName] !== Logger.Level.ERROR && this.isEnabled(Logger.Level.DEBUG) ? console.warn : console[functionName];
            if (!fn) {
                fn = console.log;
            }
            this[functionName] = fn.bind(console, '[' + levelName + '] ' + this._channel + ':');
        } else {
            this[functionName] = function () {};
        }
    }

    isEnabled(level) {
        return level <= this._logLevel;
    }

    getLevel() {
        let level = Logger.Level.ERROR;
        for (const logLevel of Logger.logLevels) {
            if (logLevel.pattern.test(this._channel)) {
                level = logLevel.level;
            }
        }
        return level;
    }
}

function parseLoggingRules(rules) {
    if (!rules) {
        return;
    }
    rules.split(/,/).map(rule => {
        const [ wildcard, level ] = rule.split(/=/);
        Logger.logLevels.push({
            pattern: new RegExp(wildcard
                .split('')
                .map(c => c === '*' ? '.*' : c.replace(/[-\/\\^$*+?.()|[\]{}]/g, "\\$&"))
                .join('')),
            level: Logger.Level[level],
        });
    });
}

// browser local storage
if (localStorage) {
    parseLoggingRules(localStorage.getItem('blueocean_logging'));
}

// node process env
if (process && process.env) {
    parseLoggingRules(process.env.blueocean_logging);
}

// browser query string
if (window && window.location && window.location.href) {
    const pattern = /.*[?&]blueocean_logging=([^&#]+).*/;
    if (pattern.test(window.location.href)) {
        parseLoggingRules(decodeURIComponent(window.location.href).replace(pattern, '$1'));
    }
}
