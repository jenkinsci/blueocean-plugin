const jsb = require('@jenkins-cd/js-builder');

jsb.gulp.on('stop', () => {
    // force exit when in debug mode
    if (typeof v8debug === 'object') {
        process.exit();
    }
});
