# Blue Ocean Pipeline Editor

This repository houses the [Jenkins](https://jenkins.io/) plugin for creating and editing Pipeline jobs within the [Blue Ocean](https://jenkins.io/projects/blueocean/) user interface.

![Pipeline Editor Screenshot](doc/editor-ss.png)

:exclamation: **Important!** This software is a work-in-progress and is not complete.

## Project Layout

(here be dragons, this may not be up-to-date, etc)

* /.storybook - Configuration files for [React Storybook](https://getstorybook.io/).
* /doc - Documentation resources
* /src
    * /main
        * /js - Some top-level things like ExtensionPoint implementations, and jenkins-js-extension.yaml metadata, etc
            * /components - Contains EditorDemo.jsx, showing an example usage of the visual editor component
                * /editor - Contains components and shared utils / types for the visual editor
            * /declarations - Flowtype declarations for externals
            * /stories - Test case stories for React Storybook
        * /less - CSS Styles, in .less format
        * /resources - Misc plugin resources. Contains index.jelly required for Jenkins HPI
