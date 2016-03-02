/*
Build file for Jenkins Blue Ocean Bootstrap theme.
*/

var autoprefixerBrowsers = [
    "Android 2.3",
    "Android >= 4",
    "Chrome >= 20",
    "Firefox >= 24",
    "Explorer >= 8",
    "iOS >= 6",
    "Opera >= 12",
    "Safari >= 6"
];

module.exports = function (grunt) {
    "use strict";

    // Force use of Unix newlines
    grunt.util.linefeed = "\n";

    // Load grunt tasks automatically. For some reason this is not default behaviour.
    require("load-grunt-tasks")(grunt);

    // Time how long tasks take. Can help when optimizing build times
    require("time-grunt")(grunt);

    grunt.initConfig({

        pkg: grunt.file.readJSON("package.json"),

        clean: {
            dist: "dist",
            licenses: "licenses"
        },

        less: {
            main: {
                options: {
                    strictMath: true,
                    sourceMap: true,
                    outputSourceFiles: true,
                    sourceMapURL: "<%= pkg.name %>-theme.css.map",
                    sourceMapFilename: "dist/css/<%= pkg.name %>-theme.css.map"
                },
                src: "less/theme.less",
                dest: "dist/css/<%= pkg.name %>-theme.css"
            }
        },

        autoprefixer: {
            options: {
                browsers: autoprefixerBrowsers
            },
            main: {
                options: {
                    map: true
                },
                src: "dist/css/<%= pkg.name %>-theme.css"
            }
        },

        copy: {
            normalize: {
                expand: true,
                cwd: "node_modules/normalize.css/",
                src: [
                    "normalize.css"
                ],
                dest: "dist/css/"
            },
            icons: {
                expand: true,
                src: [
                    "icons/*"
                ],
                dest: "dist/"
            },
            octicons: {
                expand: true,
                cwd: "node_modules/octicons/",
                src: [
                    "octicons/octicons.eot",
                    "octicons/octicons.woff",
                    "octicons/octicons.ttf",
                    "octicons/octicons.svg",
                    "octicons/*.css"
                ],
                dest: "dist/"
            },
            fontsCSS: {
                expand: true,
                cwd: "fonts/",
                src: [
                  "*.css"
                ],
                dest: "dist/css/"
            },
            fonts: {
                expand: true,
                cwd: "fonts/",
                src: [
                  "*.woff"
                ],
                dest: "dist/fonts/"
            },
            licenses: {
                expand:true,
                cwd: "node_modules/",
                src: [
                    "octicons/LICENSE.txt"
                ],
                dest: "licenses/"
            }
        },

        watch: {
            less: {
                files: "less/**/*.less",
                tasks: "less"
            }
        },
    });

    // Default task.
    grunt.registerTask("default", ["clean:dist", "less", "autoprefixer", "copy"]);
}
