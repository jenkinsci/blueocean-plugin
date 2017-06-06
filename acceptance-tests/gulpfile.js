// Using Gulp to run Nightwatch test suites from inside JUnit tests.
// Using Gulp because frontend (see maven deps) provides an easy way
// of running it.
// See NightwatchTest and it's impls.

const gulp  = require('gulp');
const shell = require('gulp-shell');
const jsdoc = require('gulp-jsdoc3');
const config = require('./jsdocConfig.json');

if (process.argv.length === 4 && process.argv[2] === '--test') {
    gulp.task('default', shell.task('nightwatch --retries 5 --suiteRetries 2 ' + process.argv[3].toString()));
} else {
    gulp.task('default', shell.task('nightwatch --retries 5 --suiteRetries 2'));
}

gulp.task('doc', function (cb) {
    console.log('***************Generate documentation+++++++++++++++++++++++++++++++++++');
    gulp.src(['README.md', 'src/**/*.js'], {read: false})
        .pipe(jsdoc(config, cb));
});
