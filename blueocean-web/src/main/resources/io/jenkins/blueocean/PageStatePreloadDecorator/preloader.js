// construct the state object parent path inside window.$blueocean.
var stateRoot = window.$blueocean = (window.$blueocean || {});
(function () {
  function setState(statePropertyPath, state) {
    var pathTokens = statePropertyPath.split('.');
    var contextObj = stateRoot;

    // Basically an Array shift
    function nextToken() {
      var nextToken = pathTokens[0];
      pathTokens = pathTokens.slice(1);
      return nextToken;
    }

    var pathToken = nextToken();

    // Construct up to, but not including, the last point in the graph.
    while (pathTokens.length !== 0) {
      if (!contextObj[pathToken]) {
        contextObj[pathToken] = {};
      }
      contextObj = contextObj[pathToken];
      pathToken = nextToken();
    }
    // And set the state on the last object on the graph.
    contextObj[pathToken] = state;
  }
  const data = JSON.parse(document.getElementById('blueocean-page-state-preload-decorator-data').textContent);
  for (const [key, value] of Object.entries(data)) {
    setState(key, value);
  }
})();
