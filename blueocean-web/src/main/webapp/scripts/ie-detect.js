(function() {
    //
    // IeVersion code from https://github.com/Gavin-Paolucci-Kleinow/ie-truth
    // License: MIT (https://github.com/Gavin-Paolucci-Kleinow/ie-truth/blob/master/LICENSE)
    //
    function IeVersion() {
        //Set defaults
        var value = {
            IsIE: false,
            IsEdge: false,
            EdgeHtmlVersion: 0,
            TrueVersion: 0,
            ActingVersion: 0,
            CompatibilityMode: false,
        };

        //Try to find the Trident version number
        var trident = navigator.userAgent.match(/Trident\/(\d+)/);
        if (trident) {
            value.IsIE = true;
            //Convert from the Trident version number to the IE version number
            value.TrueVersion = parseInt(trident[1], 10) + 4;
        }

        //Try to find the MSIE number
        var msie = navigator.userAgent.match(/MSIE (\d+)/);
        if (msie) {
            value.IsIE = true;
            //Find the IE version number from the user agent string
            value.ActingVersion = parseInt(msie[1]);
        } else {
            //Must be IE 11 in "edge" mode
            value.ActingVersion = value.TrueVersion;
        }

        //If we have both a Trident and MSIE version number, see if they're different
        if (value.IsIE && value.TrueVersion > 0 && value.ActingVersion > 0) {
            //In compatibility mode if the trident number doesn't match up with the MSIE number
            value.CompatibilityMode = value.TrueVersion != value.ActingVersion;
        }

        //Try to find Edge and the EdgeHTML version number
        var edge = navigator.userAgent.match(/Edge\/(\d+\.\d+)$/);
        if (edge) {
            value.IsEdge = true;
            value.EdgeHtmlVersion = edge[1];
        }
        return value;
    }

    //
    // Check that IE is version 11 or greater and that it's not running in
    // a compatibility mode.
    //
    // We may find that some of these compatibility modes are okay in
    // some situations. Once we do, we may be able to do more finegrained
    // stuff here.
    //
    var ieVersion = IeVersion();
    var docHead = window.document.getElementsByTagName('head')[0];
    var appurl = docHead.getAttribute('data-appurl');
    var incompatPageUrl = appurl + '/incompatibleie';
    var currentlyOnIncompatPage = window.location.pathname === incompatPageUrl;

    if (ieVersion.ActingVersion < 11 && !ieVersion.IsEdge) {
        if (!currentlyOnIncompatPage) {
            // Versions is too old and we're not already on the incompat page.
            // Redirect to the compatibility page.
            window.location = incompatPageUrl;
        }
    } else if (currentlyOnIncompatPage && !ieVersion.CompatibilityMode) {
        // We're on the incompat page but the version is OK and we're not
        // running in a compatibility mode. This should be ok, so redirect
        // back to blue ocean.
        window.location = appurl;
    }
})();
