> BlueOcean Config plugin

# BlueOcean configuration

BlueOcean configuration is injected as $blueOceanConfig JS object. It's in the following JSON format:

    {
       "version" : "1.0-alpha-7-SNAPSHOT (private-33ee8e40-vivek)",
       "jenkinsConfig" : {
          "version" : "2.2",
          "security" : {
             "authorizationStrategy" : {
                "allowAnonymousRead" : true
             },
             "enabled" : true
          }
       }
    }


# RollBar

* Enable RollBar

RollBar is disabled by default. Use BLUEOCEAN_ROLLBAR_ENABLED JVM property to enable.

```` 
mvn hpi:run -DBLUEOCEAN_ROLLBAR_ENABLED=true
```` 


## Usage ...

    try {
        foo();
        $blueocean_Rollbar.debug('foo() called');
    } catch (e) {
        $blueocean_Rollbar.error('Problem calling foo()', e);
    }
