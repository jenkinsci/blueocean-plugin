var NodeGit = require("nodegit");
var fse = require('fs-extra');
var path = require("path");
var faker = require('faker');

/**
 * Generic wrapper aroung NodeGit
 * @param pathToRepo {String}
 * @param onInit {Function}
 */
exports.init = function (pathToRepo, onInit) {
    var pathToRepo = path.resolve(pathToRepo);

    fse.emptyDirSync(pathToRepo);
    NodeGit.Repository.init(pathToRepo, 0)
        .then(function (repo) {
            var signature = NodeGit.Signature.default(repo);
            var initIndex;

            repo.refreshIndex()
                .then(function (index) {
                    initIndex = index;
                    return index.write();
                })
                .then(function (index) {
                    return initIndex.writeTree();
                })
                .then(function (oid) {
                    return repo.createCommit("HEAD", signature, signature, 'initial commit', oid, []);
                })
                .done(function () {
                    if (onInit) {
                        onInit({
                            repo: repo,
                            copyDirToRepo: function (dir) {
                                var pathToFiles = path.resolve(dir);

                                if (!fse.existsSync(pathToFiles)) {
                                    throw new Error('No such directory: ' + pathToFiles);
                                }
                                if (!fse.statSync(pathToFiles).isDirectory()) {
                                    throw new Error('Not a directory: ' + pathToFiles);
                                }

                                fse.copySync(pathToFiles, pathToRepo);
                            },
                            commit: function (message) {
                                console.log('preparing commit with repo', repo, 'and signature', signature);
                                if (!message) {
                                    message = 'commit all';
                                }

                                var index;
                                var oid;
                                var returnPromise = repo.refreshIndex()
                                    .then(function (indexResult) {
                                        index = indexResult;
                                    })
                                    .then(function () {
                                        return index.addAll();
                                    })
                                    .then(function () {
                                        return index.write();
                                    })
                                    .then(function () {
                                        return index.writeTree();
                                    })
                                    .then(function (oidResult) {
                                        oid = oidResult;
                                        return NodeGit.Reference.nameToId(repo, "HEAD");
                                    })
                                    .then(function (head) {
                                        return repo.getCommit(head);
                                    })
                                    .then(function (parent) {
                                        return repo.createCommit("HEAD", signature, signature, message, oid, [parent]);
                                    });

                                return returnPromise;
                            },
                            createRepo: function (fromDir, inDir) {
                                repo.copyDirToRepo(fromDir);
                                return repo.commit('Added ');
                            }
                        });
                    }
                });

        });
};

/**
 * create a new git repository
 * @param fromDir {String} source path
 * @param inDir {String} destination path
 */
exports.createRepo = function (fromDir, inDir) {
    return new Promise(function (resolve, reject) {
        exports.init(inDir, function (repo) {
            repo.copyDirToRepo(fromDir);
            repo.commit('Copied files from ' + fromDir)
                .then(resolve)
                .catch(reject);
        });
    });
};

/**
 * Create a new branch in a given repository
 * @param branchName {String} name of the branch
 * @param pathToRepo {String} the route to the repository
 * @returns {*}
 */
exports.createBranch = function (branchName, pathToRepo) {
    var pathToRepo = path.resolve(pathToRepo);

    return NodeGit.Repository.open(pathToRepo)
        .then(function (repo) {
            return repo.getHeadCommit()
                .then(function (commit) {
                    return repo.createBranch(
                        branchName,
                        commit, 0,
                        repo.defaultSignature(),
                        'Created "' + branchName + '" branch on HEAD');
                });
        });
};

/** 
 * Creates a file and adds it to repo.
 * 
 * FIXME: doesnt seem to actally git add the file, altohugh it does create the commit hash.
 * 
 * @param pathToRepo {String} the route to the repository
 * @param fileName {String} name of the file
 * @param options {Object} 
 * @param options.contents {string} optional contents to write to file.
 * @param options.message {string} optional commit message.
 * @returns {*}
 */
exports.createFile = function (pathToRepo, fileName, options) {
    let { contents, message } = options || {}; 

    if  (!contents) {
        contents = fileName;
    }
    return new Promise(function (resolve, reject) {
        fse.writeFile(path.join(pathToRepo, fileName), contents, function (err) {
            // when we get an error we call with error
            if (err) {
                reject(err);
            }
            // createCommit returns a promise just passing it alone
            return exports.createCommit(pathToRepo, [fileName], { message })
                .then(function (commitId) {
                    // if we reached here we have a commit
                    console.log('commitId', commitId)
                    /* We are sure that all async functions have finished.
                        * Now we let async know about it by
                        * callback without error and the commitId
                        */
                    resolve(commitId);
                })
        }); 
    });
};

/**
 * Create and return a commit promise.
 * @param pathToRepo {String} the route to the repository
 * @param files {Array} to be committed
 */
exports.createCommit = function (pathToRepo, files, options) {
    const { message: messageFromOpts } = options || {};

    return new Promise(function (resolve, reject) {
        const message = messageFromOpts || faker.lorem.sentence();
        const signatureAuthor = NodeGit.Signature.now(faker.name.findName(), faker.internet.email());
        const committerAuthor = NodeGit.Signature.now(faker.name.findName(), faker.internet.email());
        var repo, index, oid, remote;
        NodeGit.Repository.open(pathToRepo)
            .then(function (repoResult) {
                repo = repoResult;
                return repo.refreshIndex();
            })
            .then(function (indexResult) {
                index = indexResult;
                // this file is in the root of the directory and doesn't need a full path
                return files.map(function (fileName) {
                    console.log('++++Adding file +++', fileName);
                    index.addByPath(fileName);
                })
            })
            .then(function() {
                // this will write files to the index
                return index.write();
            })
            .then(function() {
                return index.writeTree();
            })
            .then(function (oidResult) {
                oid = oidResult;
                return NodeGit.Reference.nameToId(repo, "HEAD");
            })
            .then(function (head) {
                return repo.getCommit(head);
            })
            .then(function (parent) {
                return repo.createCommit("HEAD", signatureAuthor, committerAuthor, message, oid, [parent]);
            })
            .then(function (commitId) {
                console.log("New Commit: ", commitId);
                resolve(commitId);
            })
            .catch(function (err) {
                reject(err);
            })
        ;
    });

};
