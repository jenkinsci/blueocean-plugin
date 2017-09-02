package io.jenkins.graphql.api;

import graphql.schema.GraphQLObjectType;
import hudson.ExtensionList;
import hudson.ExtensionPoint;

import java.util.stream.Collectors;

public abstract class GraphQLTypeFactory implements ExtensionPoint {

    public static Iterable<GraphQLObjectType> all() {
        return ExtensionList.lookup(GraphQLTypeFactory.class)
                .stream().map(factory -> factory != null ? factory.create() : null)
                .collect(Collectors.toList());
    }

    /**
     * @return {@link GraphQLObjectType}
     */
    public abstract GraphQLObjectType create();
}
