# Changelogs

## 1.13.2 (March 6st, 2019)

* HOTFIX - [JENKINS-56383](https://issues.jenkins-ci.org/browse/JENKINS-56383) Reverted the fix for [JENKINS-38339](https://issues.jenkins-ci.org/browse/JENKINS-38339) which was causing double parallel steps to fail to render


## 1.13.1 (March 1st, 2019)

**NOTE:** blueocean-executor-info is now included in the main blueocean-plugin repository (but not as a required dependency yet)

* [JENKINS-53188](https://issues.jenkins-ci.org/browse/JENKINS-53188) New jobs created from Blue Ocean are tied with username that created them
* [JENKINS-53019](https://issues.jenkins-ci.org/browse/JENKINS-53019) Changes tab does not group changes for multiple SCM providers by SCM
* [JENKINS-52825](https://issues.jenkins-ci.org/browse/JENKINS-52825) Cannot see executors in Blue Ocean UI
* [JENKINS-53022](https://issues.jenkins-ci.org/browse/JENKINS-53022) Cannot disable a job from Blue Ocean UI
* [JENKINS-56301](https://issues.jenkins-ci.org/browse/JENKINS-56301) Restrict referrer display to same origin
* [JENKINS-38339](https://issues.jenkins-ci.org/browse/JENKINS-38339) UI for downstream jobs launched with 'build' step
* [#1920](https://github.com/jenkinsci/blueocean-plugin/pull/1920) Fix for random 404s on startup / ATH
* [#1913](https://github.com/jenkinsci/blueocean-plugin/pull/1913) Remove minimum java 11 from plugin. BO works fine in 8+

## 1.12.0, 1.13.1 (March 1st, 2019)

* Don't use, failed releases due to permission errors

## 1.11.0 (Feb 13, 2019)

* [JENKINS-54518](https://issues.jenkins-ci.org/browse/JENKINS-54518) Multibranch pipeline: Incorrect branch sort order for plain Git sources
* [JENKINS-54099](https://issues.jenkins-ci.org/browse/JENKINS-54099) Warnings about "Failed Jenkins SSE Gateway configuration request. Unknown SSE event dispatcher" in Evergreen
* [JENKINS-19022](https://issues.jenkins-ci.org/browse/JENKINS-19022) GIT Plugin (any version) heavily bloats memory use and size of build.xml with "BuildData" fields
* [JENKINS-55986](https://issues.jenkins-ci.org/browse/JENKINS-55986) “Restart Parallel Stages” appears on the screen even when the user has no permissions
* [JENKINS-55127](https://issues.jenkins-ci.org/browse/JENKINS-55127) Parallel Input stages are not individually selectable in Blue Ocean
* [JENKINS-50532](https://issues.jenkins-ci.org/browse/JENKINS-50532) Failing nested parallel stages are marked green
* [JENKINS-47286](https://issues.jenkins-ci.org/browse/JENKINS-47286) Support skipping stages in scripted pipelines for nice visualization in blue ocean and classic UI stage view
* [#1910](https://github.com/jenkinsci/blueocean-plugin/pull/1910) Update SSE client lib used in ATH
