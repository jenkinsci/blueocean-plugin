package io.jenkins.blueocean;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import io.jenkins.blueocean.rest.HttpParams;
import io.jenkins.blueocean.rest.JsonHttpResponse;
import io.jenkins.blueocean.rest.Operation;
import io.jenkins.embryo.App;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;

import java.util.List;
import java.util.Map;

/**
 * Root of Blue Ocean UI
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 **/
@Extension
public class BlueOceanUI extends App{

    public HttpResponse doHello() {
        return HttpResponses.plainText("Hello wolrd!");
    }

    public Object doRest(){

        List<App> extensions =  Jenkins.getActiveInstance().getExtensionList(App.class);
        for(App app: extensions){
            //TODO: find better way to delegate request to the right api class
            if(app.getClass().getName().equals("io.jenkins.blueocean.rest.ApiHead")){
                return app;
            }
        }
        return HttpResponses.notFound();
    }

    @Operation(method = "POST")
    public Map doTest(JsonNode jsonNode, HttpParams paramMap) {
        return ImmutableMap.of("message", jsonNode.get("message").asText(), "params", paramMap.toString());

    }

    @Operation //GET
    public HttpResponse doGreet(HttpParams paramMap) {
        if(paramMap.getFirst("name") != null) {
            return JsonHttpResponse.json(200, ImmutableMap.of("message", "Hello "+paramMap.getFirst("name")+"!"));
        }else{
            return JsonHttpResponse.json(200, ImmutableMap.of("message", "Hello World!"));

        }
    }

}
