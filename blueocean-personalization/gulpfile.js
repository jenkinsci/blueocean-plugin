//
// See https://github.com/jenkinsci/js-builder
//
var builder = require('@jenkins-cd/js-builder');

//
// Redefine the "test" task to use mocha and support es6.
// We might build this into js-builder, but is ok here
// for now.
//
builder.defineTask('test', function() {
    var mocha = require('gulp-mocha');
    var babel = require('babel-core/register');

    builder.gulp.src('src/test/js/**/*-spec.{js,jsx}')
        .pipe(mocha({
            compilers: { js: babel },
        })).on('error', function(e) {
            if (builder.isRetest()) {
                // ignore test failures if we are running retest.
                return;
            }
            throw e;
        });
});
builder.gulp.task('lint:watch', function () {
    builder.gulp.watch(['src/main/js/**/*.js', 'src/main/js/**/*.jsx'], ['lint']);
});
