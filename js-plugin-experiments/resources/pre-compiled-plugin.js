

(function($HACK){

    // This is crappy and hand-coded because I haven't yet set up babel pipeline for anything that's not include()ed

    var metadata = {
        key: "example-js-plugin",
        name: "Plain JS Plugin",
        version: "0.0.0"
    };

    $HACK.registerPlugin(metadata);

})($HACK);

