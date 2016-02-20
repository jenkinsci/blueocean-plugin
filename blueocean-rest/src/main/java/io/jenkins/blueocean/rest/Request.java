package io.jenkins.blueocean.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hudson.util.QueryParameterMap;
import io.jenkins.blueocean.commons.JsonConverter;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.router.RouteMatch;
import io.jenkins.blueocean.security.Identity;

import javax.annotation.Nonnull;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Vivek Pandey
 */
public class Request {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Request.class);

    private static final String USER_AGENT = "user-agent";

    private final Map<String, String> params;
    private final List<String> splat;
    private final QueryParameterMap queryMap;

    private final HttpServletRequest servletRequest;

    private Session session = null;

    private final Set<String> headers = new HashSet<>();

    private Reader body;

    /**
     * Constructor
     *
     * @param match   the route match
     * @param request the servlet request
     */
    @SuppressWarnings("unchecked")
    public Request(RouteMatch match, HttpServletRequest request) {
        this.servletRequest = request;
        Enumeration<String> enumeration = servletRequest.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            headers.add(enumeration.nextElement());
        }
        this.queryMap = new QueryParameterMap(request);
        List<String> requestList = Utils.convertRouteToList(match.getRequestURI());
        List<String> matchedList = Utils.convertRouteToList(match.getMatchUri());

        this.params = getParams(requestList, matchedList);
        this.splat = getSplat(requestList, matchedList);
        try {
            this.body = request.getReader();
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorExpcetion("Failed to read request input stream: "+e.getMessage(), e);
        }
        this.session = new Session(servletRequest.getSession());

    }

    /**
     * Returns the map containing all route pathParam
     *
     * @return a map containing all route pathParam
     */
    public Map<String, String> pathParam() {
        return params;
    }

    /**
     * Returns the value of the provided route pattern parameter.
     * Example: parameter 'name' from the following pattern: (get '/hello/:name')
     *
     * @param param the param
     * @return null if the given param is null or not found
     */
    public String pathParam(String param) {
        if (param == null) {
            return null;
        }

        if (param.startsWith(":")) {
            return params.get(param.toLowerCase()); // NOSONAR
        } else {
            return params.get(":" + param.toLowerCase()); // NOSONAR
        }
    }


    @SuppressWarnings("unchecked")
    public <T> T pathParam(String param, Class<T> type) {
        if (param == null) {
            return null;
        }

        String value;
        if (param.startsWith(":")) {
            value = params.get(param.toLowerCase()); // NOSONAR
        } else {
            value = params.get(":" + param.toLowerCase()); // NOSONAR
        }
        return Utils.cast(value, type);
    }

    public String queryParam(@Nonnull String key) {
        return queryParam(key, String.class, false);
    }


    public String queryParam(@Nonnull String key, boolean required) {
        return queryParam(key, String.class, required);
    }

    @SuppressWarnings("unchecked")
    public <T> T queryParam(@Nonnull String key, @Nonnull Class<T> type) {
        return queryParam(key, type, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T queryParam(@Nonnull String key, @Nonnull Class<T> type, boolean required) {
        String value = queryMap.get(key);
        if (value == null && required) {
            throw new ServiceException.BadRequestExpception(String.format("Query param %s is required", key));
        }
        if (value == null) {
            if (Boolean.class.isAssignableFrom(type)) {
                return (T) Boolean.FALSE;
            } else {
                return null;
            }
        }
        return Utils.cast(value, type);
    }

    /**
     * @return an array containing the splat (wildcard) parameters
     */
    public String[] splat() {
        return splat.toArray(new String[splat.size()]);
    }

    /**
     * @return request method e.g. GET, POST, PUT, ...
     */
    public String requestMethod() {
        return servletRequest.getMethod();
    }

    /**
     * @return the scheme
     */
    public String scheme() {
        return servletRequest.getScheme();
    }

    /**
     * @return the host
     */
    public String host() {
        return servletRequest.getHeader("host");
    }

    /**
     * @return the user-agent
     */
    public String userAgent() {
        return servletRequest.getHeader(USER_AGENT);
    }

    /**
     * @return the server port
     */
    public int port() {
        return servletRequest.getServerPort();
    }


    /**
     * @return the path info
     * Example return: "/example/foo"
     */
    public String pathInfo() {
        return servletRequest.getPathInfo();
    }

    /**
     * @return the servlet path
     */
    public String servletPath() {
        return servletRequest.getServletPath();
    }

    /**
     * @return the context path
     */
    public String contextPath() {
        return servletRequest.getContextPath();
    }

    /**
     * @return the URL string
     */
    public String url() {
        return servletRequest.getRequestURL().toString();
    }

    /**
     * @return the content type of the body
     */
    public String contentType() {
        return servletRequest.getContentType();
    }

    /**
     * @return the client's IP address
     */
    public String ip() {
        return servletRequest.getRemoteAddr();
    }

    public Identity principal(){
        Principal principal = servletRequest.getUserPrincipal();
        if(principal != null && principal instanceof Identity){
            return (Identity) principal;
        }else{
            return Identity.ANONYMOUS;
        }
    }

    public <T> T  body(@Nonnull String contentType, @Nonnull Class<T> type){
        if(contentType.toLowerCase().startsWith("application/json")){
            return JsonConverter.toJava(body, type);
        }else{
            throw new ServiceException.UnsupportedMediaTypeException("Only application/json supported at the moment");
        }
    }

    /**
     * @return the length of request.body
     */
    public int contentLength() {
        return servletRequest.getContentLength();
    }

    /**
     * Gets the value for the provided header
     *
     * @param header the header
     * @return the value of the provided header
     */
    public String headers(String header) {
        return servletRequest.getHeader(header);
    }

    /**
     * @return all headers
     */
    public Set<String> headers() {
        return ImmutableSet.copyOf(headers);
    }

    /**
     * @return the query string
     */
    public String queryString() {
        return servletRequest.getQueryString();
    }

    /**
     * Sets an attribute on the request (can be fetched in filters/routes later in the chain)
     *
     * @param attribute The attribute
     * @param value     The attribute value
     */
    public void attribute(String attribute, Object value) {
        servletRequest.setAttribute(attribute, value);
    }



    /**
     * @return all attributes
     */
    @SuppressWarnings("unchecked")
    public Set<String> attributes() {
        Set<String> attrList = new HashSet<String>();
        Enumeration<String> attributes = (Enumeration<String>) servletRequest.getAttributeNames();
        while (attributes.hasMoreElements()) {
            attrList.add(attributes.nextElement());
        }
        return attrList;
    }

    /**
     * @return the raw HttpServletRequest object handed in by servlet container
     */
    public HttpServletRequest raw() {
        return servletRequest;
    }

    /**
     * @return the query map
     */
    public QueryParameterMap queryMap() {
        return queryMap;
    }

    public boolean hasQueryParameter(){
        return servletRequest.getParameterNames().hasMoreElements();
    }

    /**
     * Returns the current session associated with this request,
     * or if the request does not have a session, creates one.
     *
     * @return the session associated with this request
     */
    public Session session() {
        return session;
    }

    /**
     * Returns the current session associated with this request, or if there is
     * no current session and <code>create</code> is true, returns  a new session.
     *
     * @param create <code>true</code> to create a new session for this request if necessary;
     *               <code>false</code> to return null if there's no current session
     * @return the session associated with this request or <code>null</code> if
     * <code>create</code> is <code>false</code> and the request has no valid session
     */
    public Session session(boolean create) {
        if (session == null) {
            HttpSession httpSession = servletRequest.getSession(create);
            if (httpSession != null) {
                session = new Session(httpSession);
            }
        }
        return session;
    }

    /**
     * @return request cookies (or empty Map if cookies aren't present)
     */
    public Map<String, String> cookies() {
        Map<String, String> result = new HashMap<String, String>();
        Cookie[] cookies = servletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                result.put(cookie.getName(), cookie.getValue());
            }
        }
        return result;
    }

    /**
     * Gets cookie by name.
     *
     * @param name name of the cookie
     * @return cookie value or null if the cookie was not found
     */
    public String cookie(String name) {
        Cookie[] cookies = servletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * @return the part of this request's URL from the protocol name up to the query string in the first line of the HTTP request.
     */
    public String uri() {
        return servletRequest.getRequestURI();
    }

    /**
     * @return Returns the name and version of the protocol the request uses
     */
    public String protocol() {
        return servletRequest.getProtocol();
    }

    /** Returns unmodifiable param map */
    private static Map<String, String> getParams(List<String> request, List<String> matched) {
        LOG.debug("get pathParam");

        Map<String, String> params = new HashMap<String, String>();

        for (int i = 0; (i < request.size()) && (i < matched.size()); i++) {
            String matchedPart = matched.get(i);
            if (Utils.isParam(matchedPart)) {
                LOG.debug("matchedPart: "
                    + matchedPart
                    + " = "
                    + request.get(i));
                params.put(matchedPart.toLowerCase(), request.get(i));
            }
        }
        return ImmutableMap.copyOf(params);
    }

    private static List<String> getSplat(List<String> request, List<String> matched) {
        LOG.debug("get splat");

        int nbrOfRequestParts = request.size();
        int nbrOfMatchedParts = matched.size();

        boolean sameLength = (nbrOfRequestParts == nbrOfMatchedParts);

        List<String> splat = new ArrayList<String>();

        for (int i = 0; (i < nbrOfRequestParts) && (i < nbrOfMatchedParts); i++) {
            String matchedPart = matched.get(i);

            if (Utils.isSplat(matchedPart)) {

                StringBuilder splatParam = new StringBuilder(request.get(i));
                if (!sameLength && (i == (nbrOfMatchedParts - 1))) {
                    for (int j = i + 1; j < nbrOfRequestParts; j++) {
                        splatParam.append("/");
                        splatParam.append(request.get(j));
                    }
                }
                splat.add(splatParam.toString());
            }
        }
        return ImmutableList.copyOf(splat);
    }

}
