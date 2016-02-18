# Jenkins
Blue Ocean is a Jenkins project that aspires to be well known and loved by Jenkins users. 
Thus, similar contributing guidelines apply as to Jenkins itself. 

For information on contributing to Jenkins, check out the https://wiki.jenkins-ci.org/display/JENKINS/contributing and https://wiki.jenkins-ci.org/display/JENKINS/Extend+Jenkins wiki pages over at the official https://wiki.jenkins-ci.org. They will help you get started with contributing to Jenkins.


## Changes and pull requests

All changes should come via pull requests, never to master directly. A pull request can be as a branch on this repo or from a forked one, doesn't matter. If a branch on this repo, name it topic/x feature/x or bug/x - depending on what you are doing. Once it is merged, please do delete the branch. 

Once a pull request is ready to be considered, @-mention the team (at least) asking for review. Mentioning an individual that may know about the area is good to (one or more). `@reviewbybees` can also be used to drag in some attention (CloudBees employees would also ideally use this for a cross review).

Once there are 1 or more +1/LGTM/:bee: or amusing positive looking emoji, the PR can be merged ideally by the author of the PR. A :bug: or a -1 means that the issues raised need addressing and should be. 

Avoid "bike shed" discussions about styles or whitespace unless it really impacts the changeset. The contributor can be encouraged to apply editor automation in future (if it is available).

Avoid opening pull requests until you think it is ready for consideration. Leave things in a feature branch otherwise. 

Squashing commits: if there are messy intermediate commits it is nice to squash things for the reviewer (but not mandatory). Always think about how to make it quick and easy for a reviewer (perhaps with more smaller PR's if needed).

# Code Style

## Java and Jenkins code

For Jenkins-ish code on the server side (which Blue Ocean is built on) - follow the same as: https://wiki.jenkins-ci.org/display/JENKINS/Beginners+Guide+to+Contributing#BeginnersGuidetoContributing-HowaboutJavacode%3F - you can make this a bit easier by importing the .editorconfig file into your favourite editor so it autoformats for you. 

## Javascript and web

(this is a bit vague while sorted out. Any help/automation appreciated)

ECMA6 is being used, comment clearly and often, keep files short. Double quotes for strings unless in imports. Use jshint. Semicolons and all the rest. 

WIP...


# Plugins and extension points

Blue Ocean is built on Jenkins, with things being plugins. Ths same applies to the UI and javascript code - extension points are embraced. Ideally all features are implemented as extensions vs in core, even if this means adding an ExtensionPoint to some code in core or another plugin. On the web, an extension point can be as simple as `<ExtensionPoint name="awesome.plugin.thing"/>` allowing others to add features to your plugin. 

# Discussion and chat 

When working on something, checkin on mailing list or chat. 

`#jenkins` on irc.freenode.net, no signup needed. 

mailing list: https://groups.google.com/forum/#!forum/jenkinsci-dev - at least until too chatty and project is kicked off and has its own list!


# Code of conduct

Same as Jenkins, applying to all discussions/comments: https://jenkins-ci.org/conduct/
