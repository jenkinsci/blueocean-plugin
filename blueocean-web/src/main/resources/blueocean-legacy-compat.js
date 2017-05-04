//"jenkins-js-module:blueocean-pipeline-editor:jenkins-js-extension:js"
(function() {
	var head = document.getElementsByTagName('head')[0];
	for (var i = 0; i < blueOceanResourceNames.length; i++) {
		var script = document.createElement('script');
		script.setAttribute('id', blueOceanResourceNames[i]);
		head.appendChild(script);
	}
})();
