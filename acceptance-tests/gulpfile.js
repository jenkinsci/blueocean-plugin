// Using Gulp to run Nightwatch test suites from inside JUnit tests.
// Using Gulp because frontend (see maven deps) provides an easy way
// of running it.
// See NightwatchTest and it's impls.

const gulp  = require('gulp');
const jsdoc = require('gulp-jsdoc3');
const config = require('./jsdocConfig.json');

gulp.task('doc', function (cb) {
    console.log('***************Generate documentation+++++++++++++++++++++++++++++++++++');
    gulp.src(['README.md', 'src/**/*.js'], {read: false})
        .pipe(jsdoc(config, cb));
});
