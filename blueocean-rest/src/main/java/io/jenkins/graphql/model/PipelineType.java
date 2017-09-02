package io.jenkins.graphql.model;

import graphql.schema.GraphQLObjectType;
import hudson.Extension;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.graphql.api.GraphQLTypeFactory;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Extension
public class PipelineType extends GraphQLTypeFactory {

    public static final String TYPE_NAME = "Pipeline";

    @Override
    public GraphQLObjectType create() {
        return newObject()
                .name(TYPE_NAME)
                .field(newFieldDefinition()
                        .name(BluePipeline.NAME)
                        .description("Name of the Pipeline")
                        .type(GraphQLString).build()
                )
                .field(newFieldDefinition()
                        .name(BluePipeline.DISPLAY_NAME)
                        .description("Display Name of the Pipeline")
                        .type(GraphQLString).build()
                )
                .field(newFieldDefinition()
                        .name(BluePipeline.FULL_NAME)
                        .description("Fully qualified Pipeline name")
                        .type(GraphQLString).build()
                )
                .field(newFieldDefinition()
                        .name(BluePipeline.FULL_DISPLAY_NAME)
                        .description("Fully qualified Pipeline display name")
                        .type(GraphQLString).build()
                )
                .build();
    }
}
