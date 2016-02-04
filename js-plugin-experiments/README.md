# Experiments to explore client-side plugin architecture

This isn't part of the maven build for now, just a place to flesh out ideas before the jenkins-side (and mvn packaging) decisions shape up some more. Putting it in the same repo so we can keep the history as things get integrated.

Initially spawned from https://github.com/vasanthk/react-es6-webpack-boilerplate

All of the infrastructure, build code an static resources (css / images etc) should be considered
 throwaway, but the JS code will probably be evolved into the real thing over time.

### Usage

```
npm install
npm start
Open http://localhost:5000
```

### Linting

ESLint with React linting options have been enabled.

```
npm run lint
```

