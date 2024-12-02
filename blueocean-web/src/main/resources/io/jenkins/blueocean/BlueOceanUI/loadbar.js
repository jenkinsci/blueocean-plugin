window.isDevelopmentMode = document.getElementById('loadbar').dataset.isDevelopmentMode === 'true';
function lb(c, t) {
  setTimeout(function () {
    document.getElementById('loadbar').classList.add(c);
  }, t);
}
lb('go', 10);
lb('long', 1000);
lb('longer', 6000);
