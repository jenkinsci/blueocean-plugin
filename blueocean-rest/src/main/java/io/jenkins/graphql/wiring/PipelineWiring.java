package io.jenkins.graphql.wiring;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldDataFetcher;
import graphql.schema.PropertyDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.graphql.api.GraphQLWiringFactory;
import io.jenkins.graphql.model.PipelineType;

import java.util.function.UnaryOperator;

public class PipelineWiring extends GraphQLWiringFactory {
    @Override
    public RuntimeWiring wire() {
        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();

//        builder.type(PipelineType.TYPE_NAME, typeWiring -> );
        builder.type(PipelineType.TYPE_NAME, new UnaryOperator<TypeRuntimeWiring.Builder>() {
            @Override
            public TypeRuntimeWiring.Builder apply(TypeRuntimeWiring.Builder builder) {
                return builder.dataFetcher(BluePipeline.FULL_NAME, new DataFetcher() {
                    @Override
                    public Object get(DataFetchingEnvironment environment) {
                        Object orgName = (String)environment.getArgument("organization");
                        Object fullName = (String)environment.getArgument(BluePipeline.FULL_NAME);

                        BlueOrganization organization = OrganizationFactory.getInstance().get(orgName);

                        BluePipelineFactory.resolve()

                        return null;
                    }
                });
            }
        });

        return builder.build();
    }
}
