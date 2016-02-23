package io.jenkins.blueocean.rest;

import hudson.Extension;
import hudson.ExtensionList;
import io.jenkins.blueocean.api.pipeline.FindPipelineRunsRequest;
import io.jenkins.blueocean.api.pipeline.FindPipelinesRequest;
import io.jenkins.blueocean.api.pipeline.GetPipelineRequest;
import io.jenkins.blueocean.api.pipeline.GetPipelineRunRequest;
import io.jenkins.blueocean.api.pipeline.PipelineService;
import io.jenkins.blueocean.api.profile.CreateOrganizationRequest;
import io.jenkins.blueocean.api.profile.CreateOrganizationResponse;
import io.jenkins.blueocean.api.profile.FindUsersRequest;
import io.jenkins.blueocean.api.profile.GetOrganizationRequest;
import io.jenkins.blueocean.api.profile.GetUserDetailsRequest;
import io.jenkins.blueocean.api.profile.GetUserRequest;
import io.jenkins.blueocean.api.profile.ProfileService;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.commons.guice.InjectLogger;
import io.jenkins.blueocean.rest.router.Route;
import io.jenkins.blueocean.rest.router.RouteContext;
import io.jenkins.blueocean.rest.router.Router;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entrypoint for blueocean REST apis. $CONTEXT_PATH/rest being root. e.g. /jenkins/rest
 *
 *
 * @author Vivek Pandey
 */
@Extension
public final class ApiHead{

    @InjectLogger
    private Logger logger;

    private final ProfileService profileService;

    private final PipelineService pipelineService;

    private final Map<String,BlueOceanRestApi> apis = new HashMap<>();

    private static final String USER_ID_PARAM=":user-id";
    private static final String ORGANIZATION_ID_PARAM=":organization-id";
    private static final String PIPELINE_ID_PARAM=":pipeline-id";
    private static final String RUN_ID_PARAM=":run-id";

    private static final String ACCEPT_TYPE_REQUEST_MIME_HEADER = "Accept";


    public ApiHead() {
        for ( BlueOceanRestApi api : ExtensionList.lookup(BlueOceanRestApi.class)) {
            apis.put(api.getUrlName(),api);
        }

        this.profileService = getService(ProfileService.class);
        this.pipelineService = getService(PipelineService.class);

        Router.get(new Route.RouteImpl(String.format("users/%s",USER_ID_PARAM)) {
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                Boolean details = request.queryParam("details", Boolean.class);

                if(details != null && details) {
                    return profileService.getUserDetails(request.principal(),
                        new GetUserDetailsRequest(request.pathParam(USER_ID_PARAM),null));
                }else {
                    return profileService.getUser(request.principal(),
                        new GetUserRequest(request.pathParam(USER_ID_PARAM)));
                }
            }
        });

        Router.get(new Route.RouteImpl(String.format("users/%s",ORGANIZATION_ID_PARAM)) {
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                return profileService.getOrganization(request.principal(),
                    new GetOrganizationRequest(request.pathParam(ORGANIZATION_ID_PARAM)));
            }
        });

        Router.get(new Route.RouteImpl(String.format("organizations/%s",ORGANIZATION_ID_PARAM)) {
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);

                return profileService.getOrganization(request.principal(),
                    new GetOrganizationRequest(request.pathParam(ORGANIZATION_ID_PARAM)));

            }
        });

        Router.post(new Route.RouteImpl("organizations") {
            @Override
            public Object handle(Request request, Response response) {
                CreateOrganizationResponse createOrganizationResponse = profileService.createOrganization(request.principal(),
                    new CreateOrganizationRequest(request.pathParam(ORGANIZATION_ID_PARAM)));
                response.status(201);
                response.header("Location", appendSlashToPath(request.uri())+createOrganizationResponse.organization);
                return createOrganizationResponse;
            }
        });

        Router.get(new Route.RouteImpl(String.format("organizations/%s/pipelines", ORGANIZATION_ID_PARAM)){
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                return pipelineService.findPipelines(request.principal(),
                    new FindPipelinesRequest(request.pathParam(ORGANIZATION_ID_PARAM), null));
            }
        });


        Router.get(new Route.RouteImpl(String.format("organizations/%s/pipelines/%s",
            ORGANIZATION_ID_PARAM, PIPELINE_ID_PARAM)){
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                return pipelineService.getPipeline(request.principal(),
                    new GetPipelineRequest(request.pathParam(ORGANIZATION_ID_PARAM),
                        request.pathParam(PIPELINE_ID_PARAM)));
            }
        });

        Router.get(new Route.RouteImpl(String.format("organizations/%s/pipelines/%s/runs",
            ORGANIZATION_ID_PARAM, PIPELINE_ID_PARAM)){
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);

                return pipelineService.findPipelineRuns(request.principal(),
                    new FindPipelineRunsRequest(request.pathParam(ORGANIZATION_ID_PARAM),
                        request.pathParam(PIPELINE_ID_PARAM),
                        request.queryParam("latestOnly", Boolean.class),null,null,null));
            }
        });


        Router.get(new Route.RouteImpl(String.format("organizations/%s/pipelines/%s/runs/%s",
            ORGANIZATION_ID_PARAM, PIPELINE_ID_PARAM, RUN_ID_PARAM)){
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);

                return pipelineService.getPipelineRun(request.principal(),
                    new GetPipelineRunRequest(request.pathParam(ORGANIZATION_ID_PARAM),
                        request.pathParam(PIPELINE_ID_PARAM), request.pathParam(RUN_ID_PARAM)));
            }
        });
        Router.get(new Route.RouteImpl("search"){
            @Override
            @SuppressWarnings({"unchecked"})
            public Object handle(Request request, Response response) {
                response.status(200);

                Query query = Query.parse(request.queryParam("q", true));
                assert query != null; //will never be the case since its taken care in request.queryParam()
                switch (query.type){
                    case "user":
                        return profileService.findUsers(request.principal(),
                            new FindUsersRequest(query.param("organization", true),
                                query.param("start", Long.class),
                                query.param("limit", Long.class)));
                    case "pipeline":
                        return pipelineService.findPipelines(request.principal(),
                            new FindPipelinesRequest(query.param("organization", true), query.param("pipeline")));
                    case "run":
                        return pipelineService.findPipelineRuns(request.principal(),
                            new FindPipelineRunsRequest(query.param("organization", true),
                                query.param("pipeline"),
                                query.param("latestOnly", Boolean.class),
                                query.param("branches", List.class),
                                query.param("start", Long.class),
                                query.param("limit", Long.class)));
                    default:
                        throw new ServiceException.BadRequestExpception("Unknown query type: "+query.type);
                }
            }
        });
    }

    /**
     * Exposes all {@link BlueOceanRestApi}s to URL space.
     */
    public BlueOceanRestApi getDynamic(String route) {
        return apis.get(route);
    }

    public HttpResponse doDynamic(StaplerRequest request, StaplerResponse response){
        String method = request.getMethod().toLowerCase();
        String url = request.getOriginalRestOfPath();
        String acceptType = request.getHeader(ACCEPT_TYPE_REQUEST_MIME_HEADER);
        Body body = new Body();
        HttpMethod httpMethod = HttpMethod.get(method);

        RouteContext context = RouteContext.create().withAcceptType(acceptType).withBody(body)
            .withHttpMethod(httpMethod).withMatcher(Router.INSTANCE.routeMatcher())
            .withResponse(new Response(response)).withUri(url).withHttpRequest(request);

        try{
            Router.execute(context);
            return JsonHttpResponse.json(context.body().get());
        }catch (ServiceException e){
            if(e.status > 499) {
                logger.error(e.getMessage(), e);
            }
            return JsonHttpResponse.json(e.errorMessage, e.status);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            return JsonHttpResponse.json(new ServiceException.ErrorMessage(500, "Unexpected error"), 500);
        }
    }

    private <T> T getService(Class<T> type){
        ExtensionList<T> services = Jenkins.getActiveInstance().getExtensionList(type);
        if(services.isEmpty()){
            throw new ServiceException.UnexpectedErrorExpcetion("No implementation found for service API: " + type);
        }
        return services.get(0);
    }

    private String appendSlashToPath(String path){
        return path.endsWith("/") ? path : path + "/";
    }

}
