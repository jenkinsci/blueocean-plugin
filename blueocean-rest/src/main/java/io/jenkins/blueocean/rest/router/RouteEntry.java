package io.jenkins.blueocean.rest.router;

import io.jenkins.blueocean.rest.HttpMethod;
import io.jenkins.blueocean.rest.Utils;

import java.util.List;

/**
 * @author Per Wendel
 */
class RouteEntry {

    final HttpMethod httpMethod;
    final String path;
    final String acceptedType;
    final Route target;

    public RouteEntry(HttpMethod httpMethod, String path, String acceptedType, Route target) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.acceptedType = acceptedType;
        this.target = target;
    }

    boolean matches(HttpMethod httpMethod, String path) {
        if ((httpMethod == HttpMethod.before || httpMethod == HttpMethod.after)
            && (this.httpMethod == httpMethod)
            && this.path.equals(Utils.ALL_PATHS)) {
            // Is filter and matches all
            return true;
        }
        boolean match = false;
        if (this.httpMethod == httpMethod) {
            match = matchPath(path);
        }
        return match;
    }

    private boolean matchPath(String path) {
        if (!this.path.endsWith("*")
            && ((path.endsWith("/") && !this.path.endsWith("/"))
            || (this.path.endsWith("/") && !path.endsWith("/")))) {
            // One and not both ends with slash
            return false;
        }
        if (this.path.equals(path)) {
            // Paths are the same
            return true;
        }

        // check pathParam
        List<String> thisPathList = Utils.convertRouteToList(this.path);
        List<String> pathList = Utils.convertRouteToList(path);

        int thisPathSize = thisPathList.size();
        int pathSize = pathList.size();

        if (thisPathSize == pathSize) {
            for (int i = 0; i < thisPathSize; i++) {
                String thisPathPart = thisPathList.get(i);
                String pathPart = pathList.get(i);

                if ((i == thisPathSize - 1) && (thisPathPart.equals("*") && this.path.endsWith("*"))) {
                    // wildcard match
                    return true;
                }

                if ((!thisPathPart.startsWith(":"))
                    && !thisPathPart.equals(pathPart)
                    && !thisPathPart.equals("*")) {
                    return false;
                }
            }
            // All parts matched
            return true;
        } else {
            // Number of "path parts" not the same
            // check wild card:
            if (this.path.endsWith("*")) {
                if (pathSize == (thisPathSize - 1) && (path.endsWith("/"))) {
                    // Hack for making wildcards work with trailing slash
                    pathList.add("");
                    pathList.add("");
                    pathSize += 2;
                }

                if (thisPathSize < pathSize) {
                    for (int i = 0; i < thisPathSize; i++) {
                        String thisPathPart = thisPathList.get(i);
                        String pathPart = pathList.get(i);
                        if (thisPathPart.equals("*") && (i == thisPathSize - 1) && this.path.endsWith("*")) {
                            // wildcard match
                            return true;
                        }
                        if (!thisPathPart.startsWith(":")
                            && !thisPathPart.equals(pathPart)
                            && !thisPathPart.equals("*")) {
                            return false;
                        }
                    }
                    // All parts matched
                    return true;
                }
                // End check wild card
            }
            return false;
        }
    }

    public String toString() {
        return httpMethod.name() + ", " + path + ", " + target;
    }
}
