package io.jenkins.blueocean.rest;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.security.csrf.CrumbExclusion;
import io.jenkins.blueocean.RootRoutable;

/**
 * This class forces the Blueocean API to require json for POSTs so that we do not need a crumb.
 * @author Ivan Meredith
 */
@Extension
public class APICrumbExclusion extends CrumbExclusion{
    @Override
    public boolean process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws IOException, ServletException {
        String pathInfo = httpServletRequest.getPathInfo();

        for (RootRoutable r : ExtensionList.lookup(RootRoutable.class)) {
            String path = getExclusionPath(r.getUrlName());
            if (pathInfo != null && pathInfo.startsWith(path)) {
                String header = httpServletRequest.getHeader("Content-Type");
                if(header != null && header.contains("application/json")) {
                    filterChain.doFilter(httpServletRequest, httpServletResponse);
                    return true;
                } else {
                    return false;
                }

            }
        }

        return false;

    }

    public String getExclusionPath(String route) {
        return "/blue/" + route + "/";
    }

}
