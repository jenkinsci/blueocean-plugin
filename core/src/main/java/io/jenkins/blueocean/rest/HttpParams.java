package io.jenkins.blueocean.rest;

import java.util.Map;

/**
 * @author Vivek Pandey
 **/
public class HttpParams {
    private final Map<String,String[]> params;

    public HttpParams(Map<String,String[]> params) {
        this.params = params;
    }

    public String getFirst(String name){
        String[] val = params.get(name);
        if(val == null || val.length == 0){
            return null;
        }
        return val[0];
    }

    public String[] get(String name){
        return params.get(name);
    }

    @Override
    public String toString() {
        return params.toString();
    }
}
