# Jenkins
Blue Ocean is a Jenkins project that aspires to be well known and loved by Jenkins users. 
Thus, similar contributing guidelines apply as to Jenkins itself. 

For information on contributing to Jenkins, check out the https://wiki.jenkins-ci.org/display/JENKINS/Beginners+Guide+to+Contributing and https://wiki.jenkins-ci.org/display/JENKINS/Extend+Jenkins wiki pages over at the official https://wiki.jenkins-ci.org. They will help you get started with contributing to Jenkins.


## Changes and pull requests

All changes should come via pull requests, never to master directly. A pull request can be as a branch on this repo or from a forked one, doesn't matter. If a branch on this repo, name it topic/x feature/x or bug/x - depending on what you are doing. Once it is merged, please do delete the branch. 

You can open a Pull Request at any time. People often like to do this because it allows them to utilize the Pull Request Builder on their PR, as well as allowing them to capture early feedback.

If the PR:

* Is still a "Work In Progress" (i.e. incomplete), then please mark it with the "__work-in-progress__" label (GitHub label).
* Ready for review, then please mark it with the "__needs-review__" label (GitHub label). You should also @-mention the team (at least) that you'd like to get the review from. Mentioning an individual that may know about the area is good to (one or more). `@reviewbybees` can also be used to drag in some attention (CloudBees employees would also ideally use this for a cross review).

> __Note__: It's perfectly fine to use both of the above labels at the same time. However, if the PR is still a __work-in-progress__, it's a good idea to list the tasks that are done and the tasks still todo (using a [GitHub checklist](https://github.com/blog/1375-task-lists-in-gfm-issues-pulls-comments)), giving reviewers a clear idea of where the PR is. 

Once the PR is no longer a __work-in-progress__, is building fine (according to the Pull Request Builder) and has 1 or more +1/LGTM/:bee: or amusing positive looking emoji, the PR can be merged ideally by the author of the PR. A :bug: or a -1 means that the issues raised need addressing and should be. 

Avoid "bike shed" discussions about styles or whitespace unless it really impacts the changeset. The contributor can be encouraged to apply editor automation in future (if it is available).

# Merging

All commits should be squashed before being merged to `master` to preserve a sane history. The Merge button on the PR screen has been configured to do this automatically.

# Code Style

## Java and Jenkins code

For Jenkins-ish code on the server side (which Blue Ocean is built on) - follow the same as: https://wiki.jenkins-ci.org/display/JENKINS/Beginners+Guide+to+Contributing#BeginnersGuidetoContributing-HowaboutJavacode%3F - you can make this a bit easier by importing the .editorconfig file into your favourite editor so it autoformats for you. 

## JavaScript and web

(this is a bit vague while sorted out. Any help/automation appreciated)

ECMA6 is being used, comment clearly and often, keep files short. Double quotes for strings unless in imports. Use jshint. Semicolons and all the rest. 

WIP...


# Plugins and extension points

Blue Ocean is built on Jenkins, with things being plugins. Ths same applies to the UI and javascript code - extension points are embraced. Ideally all features are implemented as extensions vs in core, even if this means adding an ExtensionPoint to some code in core or another plugin. On the web, an extension point can be as simple as `<Extensions.Renderer name="awesome.plugin.thing"/>` allowing others to add features to your plugin (you can grep through existing plugins for examples of extension points).

# Discussion and chat 

When working on something, checkin on mailing list or chat. 

`#jenkins` on irc.freenode.net, no signup needed. 

mailing list: https://groups.google.com/forum/#!forum/jenkinsci-dev - at least until too chatty and project is kicked off and has its own list!


# Code of conduct

Same as Jenkins, applying to all discussions/comments: https://jenkins-ci.org/conduct/
