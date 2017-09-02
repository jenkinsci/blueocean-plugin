package io.jenkins.graphql.api;

import graphql.schema.idl.RuntimeWiring;
import hudson.ExtensionList;
import hudson.ExtensionPoint;

public abstract class GraphQLWiringFactory implements ExtensionPoint {

    public static Iterable<GraphQLWiringFactory> all() {
        return ExtensionList.lookup(GraphQLWiringFactory.class);
    }

    public abstract RuntimeWiring wire();
}
