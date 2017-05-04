(function(){
	var exports = {};
	function require(name) {
		if (!exports[name]) {
			var module = { exports: {} };
			var fn = requires[name];
			if (!fn) {
				
			}
			fn(require, module, module.exports);
			exports[name] = module;
		}
		return exports[name];
	}
	var rkeys = Object.keys(requires);
	for (var i = 0; i < rkeys.length; i++) {
		require(rkeys[i]);
	}
})();
