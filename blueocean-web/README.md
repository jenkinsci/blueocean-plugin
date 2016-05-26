# Web middleware

This module mostly contains middleware for serving up "/blue" GUI. 
In general, you shouldn't need to modify stuff in for plugins or features as it is infrastructure. Move along. 

Look for blueocean.js for excitement. 

## how this works with Javascript

Jenkins-js-modules and friends are used to power this. Look in `src/main/js` in any plugin. 

If you wish to make changes to blueocean.js in this plugin, then you will need to install gulp (http://gulpjs.com/), and then run `cd blueocean-plugin && mvn hpi:run` in a separate terminal. 

Then run:

``` 
$ gulp bundle:watch
```

(or run gulp, after each change) in this directory. This will pick up any changes. 
If you are editing any other UI modules, run the same in their respective directories. 
