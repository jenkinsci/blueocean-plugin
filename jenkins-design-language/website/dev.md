## Developing and testing within JDL

Development of JDL components without accompanying Jenkins plugins and related overhead is
facilitated by use of [react-storybook]:

```bash
$ npm run storybook
```

This will startup a [local webpack server](http://localhost:9001/) that watches files for rapid turnaround.
Stories are located in [/src/js/stories/](src/js/stories/) and allow you to develop and test example use cases for
components within JDL. Note that Storybook doesn't currently watch and rebuild the CSS from .less files, only
JavaScript sources. If you're making changes to styles as well as JS, run the `watch-styles` Gulp task:

```bash
$ npm run gulp watch-styles
```

To read more about [react-storybook], please see [Introducing React Storybook](https://medium.com/@arunoda/ec27f28de1e2)
on Medium.

## Developing and testing alongside Blue Ocean

If you are working on adding features to the JDL, you'll probably want to do that in an iterative
way i.e you will not want to have to publish changes after every change (or use relative `package.json` paths)
and then run `npm install`. We recommend using [slink](https://github.com/tfennelly/slink) to avoid potential issues related to `npm install`.

### Step 1: Install slink

```bash
$ npm install -g slink
```

### Step 2: Locally `install` your JDL checkout into blueocean-web and any other modules

Assuming you're in a directory containing clones of JDL and Blue Ocean:

```bash
$ cd blueocean/blueocean-web/
$ npm install ../../jenkins-design-language/
$ cd ../blueocean-dashboard/
$ npm install ../../jenkins-design-language/
```

This will perform a local install that overrides the released JDL version that was installed
from npm's servers when building Blue Ocean. Don't change your `package.json` to refer
to the local version.

**Note:** Because we're omitting the `--save` option, this is a temporary operation.
It can be easily undone by running `npm install` in the target project directories,
or by running `mvn clean install` in the root of your Blue Ocean checkout.

### Step 3: `watch` for JDL code changes

In the `jenkins-design-language` folder, run `gulp watch`.

### Step 4: Run slink for each module

```bash
$ cd blueocean/blueocean-web/
$ slink ../../jenkins-design-language/
```
You should see something like `Watching for changes in /Users/jdoe/work/jenkins-design-language`.

This will need to be done for any other modules related to your testing. *In another terminal:*

```bash
$ cd blueocean/blueocean-dashboard/
$ slink ../../jenkins-design-language/
```
It may seem strange that you need to do this, but there's a good reason. Blue Ocean does not load and
instantiate multiple instances of `react` (and `react-dom`) and `@jenkins-cd/design-language` for every plugin. That simply
would not work (for a number of reasons). Instead, `blueocean-web` is responsible for creating and `export`ing
shared instances of these modules via [js-modules]. Then, other plugins that need access to these shared component
instances `import` them via [js-modules]. For that reason, `blueocean-web` needs to know about ("watch for")
changes in the `@jenkins-cd/design-language` as you are working on it.

### Step 5: `watch` for `blueocean-web` changes (and rebundle)

In the `blueocean-web` folder, run `gulp bundle:watch`.

This will watch for changes not only in `blueocean-web` code, but also in the linked `@jenkins-cd/design-language`
code, triggering a rebundle of `blueocean-web` code, making it available to the browser.

> As you might notice, there's a `watch` + `rebundle` cascading effect going on here i.e. changes in
> `@jenkins-cd/design-language` are watched for and rebundled (Step 2), which triggers the `blueocean-web`
> watch and rebundle (Step 4).

### Step 6-n: Install and slink the JDL code into other HPI plugin projects + `bundle:watch` there too

Basically ... repeating Steps 2, 4, and 5 in other HPI plugins that you are working on.

```bash
$ cd blueocean/blueocean-dashboard/
$ npm run gulp bundle:watch
```

### Flow

We're using Facebook's [Flow](http://flowtype.org/) type system to help us spot bugs
and improve documentation within the JDL codebase. If you're on using a supported
platform (basically not Windows), please do the same. We're hoping it will continue
to aid in maintaining quality code, and will almost certainly speed up the pull-request
process for all involved.

### Publish a new version of `@jenkins-cd/design-language`

Once your changes have been made (and through the PR contribution process), we need to publish a new version
of the `@jenkins-cd/design-language` package to the NPM registry (to the `jenkins-cd` organization).

If you're not already a member of the `jenkins-cd` organization, please ping us and we'll add you. Otherwise,
someone else can publish a new version of the package for you.

## Repository History

This current repository was filtered from https://github.com/cloudbees/blueocean.git at 99f14895c448eb7303d2eaa29f369ce50f4fb674
on 2016-05-09.

[React]: https://reactjs.org/
[js-builder]: https://github.com/jenkinsci/js-builder
[js-samples]: https://github.com/jenkinsci/js-samples
[js-modules]: https://github.com/jenkinsci/js-modules
[react-storybook]: https://github.com/kadirahq/react-storybook