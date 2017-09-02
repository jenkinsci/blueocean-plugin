package io.jenkins.graphql.http;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.servlet.GraphQLServlet;
import graphql.servlet.SimpleGraphQLServlet;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import io.jenkins.graphql.api.GraphQLTypeFactory;
import io.jenkins.graphql.api.GraphQLWiringFactory;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import java.io.IOException;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

@Extension
@Restricted(NoExternalUse.class)
public class GraphQLAction implements UnprotectedRootAction {
    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "graph";
    }

    public void doDynamic(StaplerRequest req, StaplerResponse resp) throws ServletException, IOException {
        // Register all types
        TypeDefinitionRegistry typeDefinitionRegistry = new TypeDefinitionRegistry();
        GraphQLTypeFactory.all().forEach(t -> { typeDefinitionRegistry.add(t.getDefinition()); });
        // Wire all types
        RuntimeWiring.Builder wiringBuilder = newRuntimeWiring();
        GraphQLWiringFactory.all().forEach(w -> { wiringBuilder.wiringFactory(w.wire().getWiringFactory()); });
        // Generate schema
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema schema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, wiringBuilder.build());

        // Serve Graph
        GraphQLServlet servlet = new SimpleGraphQLServlet(schema);
        servlet.service(req, resp);
    }
}
