// this script should not require any non-core-modules,
// so it could be called to bootstrap other modules,
// without requiring npm modules in projectBuilder
// (i.e.: without npm install in projectBuilder)
var
  fs = require('fs'),
  path = require('path'),
  execFile = require('child_process').execFile,
  execFileSync = require('child_process').execFileSync,
  semver = require('semver');

var skipGitDependencies = process.env.SKIP_GIT_DEPS === 'true';

var projectRoot = path.resolve(__dirname);
var globalNpmCacheFolder = path.join(projectRoot, '.npmcache');
var installedCacheFile = path.join(projectRoot, 'installedCache.tmp');

var installedCache = {};

var installedCacheFileExists = fs.existsSync(installedCacheFile);
if (installedCacheFileExists) {
  try {
    installedCache = JSON.parse(fs.readFileSync(installedCacheFile));
  } catch (e) {}
}

function isNpmInstallNeeded(modulePath) {
  var modulesThatShouldExist = [];
  var packageJson = require(path.join(modulePath, 'package.json'));
  var nodeModulesPath = path.join(modulePath, 'node_modules');
  var allDeps = {};

  if (packageJson.blueoceanDependencies) {
    for (var tDep in packageJson.blueoceanDependencies) {
      if (modulesThatShouldExist.indexOf(tDep) < 0) {
        modulesThatShouldExist.push(tDep);
        allDeps[tDep] = '*';
      }
    }
  }

  if (packageJson.devDependencies) {
    for (var devDep in packageJson.devDependencies) {
      if (modulesThatShouldExist.indexOf(devDep) < 0) {
        modulesThatShouldExist.push(devDep);
        allDeps[devDep] = packageJson.devDependencies[devDep];
      }
    }
  }

  if (packageJson.dependencies) {
    for (var dep in packageJson.dependencies) {
      if (modulesThatShouldExist.indexOf(dep) < 0) {
        modulesThatShouldExist.push(dep);
        allDeps[dep] = packageJson.dependencies[dep];
      }
    }
  }

  if (!fs.existsSync(nodeModulesPath)) {
    return true;
  }

  var filtered = fs.readdirSync(nodeModulesPath).filter(function(file) {
    return fs.statSync(path.join(nodeModulesPath, file)).isDirectory() && modulesThatShouldExist.indexOf(file) >= 0;
  });

  if (filtered.length < modulesThatShouldExist.length) {
    return true;
  }

  for (var d in allDeps) {
    var packageJsonIs = require(path.join(nodeModulesPath, d, 'package.json'));
    var versionOfDepIs = packageJsonIs.version;
    var versionOfDepShould = allDeps[d];

    var isGit = versionOfDepShould.indexOf('git') >= 0 || versionOfDepShould.indexOf('/') >= 0;

    if (isGit) {
      if (skipGitDependencies) {
        continue;
      }

      var ref = '';
      if (versionOfDepShould.indexOf('#') > 0) {
        ref = versionOfDepShould.substring(versionOfDepShould.indexOf('#') + 1);
      }

      var repo = packageJsonIs._resolved.substring(0, packageJsonIs._resolved.indexOf('#'));

      if (!repo) {
        return true;
      }

      var isGitHead = packageJsonIs.gitHead;
      // git ls-remote repo HEAD ref
      var shouldGitHead = getRemoteCommitHash(repo, ref);

      if (isGitHead !== shouldGitHead) {
        return true;
      }
    } else if (!semver.satisfies(versionOfDepIs, versionOfDepShould)) {
      return true;
    }
  }

  return false;
}

function getRemoteCommitHash(repo, ref) {
  repo = repo.replace('git+https', 'git');

  var res = execFileSync('git', ['ls-remote', repo, 'HEAD', ref]);
  var output = res.toString();
  return output.substring(0, output.indexOf('\t'));
}

/**
 * run npm install in a given location
 *
 * @param {string} location current working directory where to run npm install
 * @param callback
 */
function npmInstall(location, callback) {
  execFile('npm', ['install', '--cache=' + globalNpmCacheFolder], {
    cwd: location
  }, callback);
}

/**
 * Creates a two dimensional array of the key-value pairs for object, e.g. [[key1, value1], [key2, value2]].
 *
 * this is a helper function cloning _.pairs, as this script must get along without external dependencies
 * @param object
 */
function pairs(object) {
  var array = [];

  for (var property in object) {
    if (object.hasOwnProperty(property)) {
      array.push([property, object[property]]);
    }
  }

  return array;
}

function rmdir (dir) {
	var list = fs.readdirSync(dir);
	for(var i = 0; i < list.length; i++) {
		var filename = path.join(dir, list[i]);
		var stat = fs.statSync(filename);

		if (filename === '.' || filename === '..') {
			// pass these files
		} else if (stat.isDirectory()) {
			// rmdir recursively
			rmdir(filename);
		} else {
			// rm fiilename
			fs.unlinkSync(filename);
		}
	}
	fs.rmdirSync(dir);
};

function link (location, symlinkDestination, dependency, dependencies, callback) {
  var modulePath = path.join(projectRoot, dependency[1]);

  console.log('link ' + dependency[1] + ' into ' + path.relative(projectRoot, location));

  fs.symlink(path.relative(path.join(process.cwd(), 'node_modules') , modulePath), symlinkDestination, 'dir', function (err) {
    if (err && err.code !== 'EEXIST') {
      return callback(err);
    }
    // go to the just 'installed'/symlinked module and call npm install

    if (installedCache[dependency[0]]) {
      console.log('skipping ' + dependency[0] + ', already installed before');
      return linkDependency(location, dependencies, callback);
    }

    installedCache[dependency[0]] = dependency[1];

    // check if npm install is needed
    if (!isNpmInstallNeeded(symlinkDestination)) {
      console.log('skipping ' + dependency[0] + ', package.json fulfilled');
      return linkDependency(location, dependencies, callback);
    }

    console.log('installing ' + symlinkDestination + '...');
    npmInstall(symlinkDestination, function() {
      // call installTaibikaDependencies for the just installed module
      // to install dependencies recursively, as blueoceanDependencies might not have 'run link' configuration
      // linkBlueoceanDependencies(symlinkDestination, function () {
        linkDependency(location, dependencies, callback);
      // });
    });
  });
}

/**
 * install a list of (local) dependencies into a given location
 *
 * @param {string} location working directory where to call "npm install" (relative to projectRoot)
 * @param {Array} dependencies list of tuples with (dependency-name, relativeLocation), used as a queue
 * @param {function} callback callback to execute when all dependencies have been installed
 */
function linkDependency(location, dependencies, callback) {
  if (dependencies.length > 0) {
    var dependency = dependencies.shift();

    var modulePath = path.join(projectRoot, dependency[1]);
    // we fetch the name from the package.json, as it is more accurate than using the folder name
    var moduleName = require(path.join(modulePath, 'package.json')).name;

    var nodeModules = path.join(location, 'node_modules');

    // 'node_modules' might not yet exist (e.g.: npm install did not yet run, or there are no dependencies beside
    // blueoceanDependencies at all
    fs.mkdir(nodeModules, function (err) {
      if (err && err.code !== 'EEXIST') {
        return callback(err);
      }
      var symlinkDestination = path.join(nodeModules, moduleName);

      fs.exists(symlinkDestination, function (exists) {
        if (exists) {
          fs.lstat(symlinkDestination, function (err, stats) {
            if (err) return callback(err);
            if (!stats.isSymbolicLink()) {
              // delete first
              rmdir(symlinkDestination);
            }
            link(location, symlinkDestination, dependency, dependencies, callback);
          });
        } else {
          link(location, symlinkDestination, dependency, dependencies, callback);
        }
      });
    });
  }
  else {
    callback();
  }
}

/**
 * install blueoceanDependencies from package.json
 * this is done recursively, i.e. declared blueoceanDependencies are checked
 * for blueoceanDependencies,
 *
 * @param {string} location
 * @param callback
 */
function linkBlueoceanDependencies(location, callback) {
  var expectedPackageJsonLocation = path.join(location, 'package.json');
  fs.exists(expectedPackageJsonLocation, function (packageJsonExists) {
    if (packageJsonExists) {
      var packageJsonContent = require(expectedPackageJsonLocation);

      if (packageJsonContent.hasOwnProperty('blueoceanDependencies')) {
        // transform the dependency map into an array with [key, value] pairs, so we can use it as a queue
        var dependencies = pairs(packageJsonContent.blueoceanDependencies);

        // install each dependency one after the other (no asynchronous execution)
        linkDependency(location, dependencies, callback);
      }
      else {
        // no blueoceanDependencies in package.json, nothing to do here!
        callback();
      }
    }
    else {
      // no package.json found: nothing to do here (why has this function been called in the first place?)
      callback();
    }
  });
}

console.log( process.cwd(), process.argv.slice(2))
// call the main function iff this file is called directly (not required by any other module)
if (require.main === module) {
  main.apply(process.argv.slice(2));
}
/**
 * main function to execute if the script is executed directly
 * @param {string} [directory] directory where to look for package.json
 */
function main(directory) {
  var d = directory || process.cwd();
  linkBlueoceanDependencies(d, function (err) {
    if (err) return console.log(err);
    console.log('Finished linking blueoceanDependencies in "' + d.substr(projectRoot.length) + '".');

    if (installedCacheFileExists) {
      try {
        var newContent = JSON.parse(fs.readFileSync(installedCacheFile));
        for (var m in installedCache) {
          newContent[m] = installedCache[m];
        }
        fs.writeFileSync(installedCacheFile, JSON.stringify(newContent));
      } catch (e) {}
    }
  });
}

module.exports = {
  linkBlueoceanDependencies: linkBlueoceanDependencies
};
