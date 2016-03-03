"use strict";
/*
    Build file for Jenkins Design Language theme.
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
                    sourceMapFilename: "dist/assets/css/<%= pkg.name %>-theme.css.map"
                },
                src: "less/theme.less",
                dest: "dist/assets/css/<%= pkg.name %>-theme.css"
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
                src: "dist/assets/css/<%= pkg.name %>-theme.css"
            }
        },

        copy: {
            normalize: {
                expand: true,
                cwd: "node_modules/normalize.css/",
                src: [
                    "normalize.css"
                ],
                dest: "dist/assets/css/"
            },
            icons: {
                expand: true,
                src: [
                    "icons/*"
                ],
                dest: "dist/assets/"
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
                dest: "dist/assets/"
            },
            fontsCSS: {
                expand: true,
                cwd: "fonts/",
                src: [
                    "*.css"
                ],
                dest: "dist/assets/css/"
            },
            fonts: {
                expand: true,
                cwd: "fonts/",
                src: [
                    "*.woff"
                ],
                dest: "dist/assets/fonts/"
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

        babel: {
            options: {
                plugins: ["transform-react-jsx"],
                presets: ["es2015", "react"]
                // TODO: presets: ["es2015", "react", "stage-0"]
            },
            jsx: {
                files: [{
                    expand: true,
                    cwd: "src/js/",
                    src: ["**/*.{js,jsx}"],
                    dest: "dist/js/",
                    ext: ".js"
                }]
            }
        },

        watch: {
            // TODO: Watch src, run babel.
            less: {
                files: "less/**/*.less",
                tasks: "less"
            }
        },
    });

    // Default task.
    grunt.registerTask("default", ["clean:dist", "babel", "less", "autoprefixer", "copy"]);
}
