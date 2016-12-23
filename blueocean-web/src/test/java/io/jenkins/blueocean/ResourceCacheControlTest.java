/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.blueocean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ResourceCacheControlTest {

    private ResourceCacheControl resourceCacheControl;
    private HttpServletRequest servletRequest;
    private HttpServletResponse servletResponse;
    private FilterChain filterChain;

    @Test
    public void test_addPath() {
        ResourceCacheControl resourceCacheControl = new ResourceCacheControl();

        Assert.assertEquals("/a/b/", resourceCacheControl.addPath("a/b"));
        Assert.assertEquals("/a/b/", resourceCacheControl.addPath("/a/b"));
        Assert.assertEquals("/a/b/", resourceCacheControl.addPath("a/b/"));
        Assert.assertEquals("/a/b/", resourceCacheControl.addPath("/a/b/"));
    }

    @Before
    public void before() throws IOException, ServletException {
        resourceCacheControl = new ResourceCacheControl();

        resourceCacheControl.addPath("a/b");
        resourceCacheControl.addPath("c/d");

        servletRequest = Mockito.mock(HttpServletRequest.class);
        servletResponse = Mockito.mock(HttpServletResponse.class);
        filterChain = Mockito.mock(FilterChain.class);
    }

    @Test
    public void test_cache_control_set() throws IOException, ServletException {
        Mockito.when(servletRequest.getPathInfo()).thenReturn("/a/b/c.js");
        resourceCacheControl.doFilter(servletRequest, servletResponse, filterChain);
        Mockito.verify(servletResponse).setHeader("Cache-Control", "public, max-age=31536000");
    }

    @Test
    public void test_cache_control_not_set() throws IOException, ServletException {
        Mockito.when(servletRequest.getPathInfo()).thenReturn("/a/bc.js");
        resourceCacheControl.doFilter(servletRequest, servletResponse, filterChain);
        Mockito.verify(servletResponse, Mockito.never()).setHeader("Cache-Control", "public, max-age=31536000");
    }

    @Test
    public void test_getPathInfo_null_JENKINS_40116() throws IOException, ServletException {
        Mockito.when(servletRequest.getPathInfo()).thenReturn(null);
        Assert.assertFalse(resourceCacheControl.isCacheableResourceRequest(servletRequest));
    }

    @Test
    public void test_getPathInfo_not_null_JENKINS_40116() throws IOException, ServletException {
        Mockito.when(servletRequest.getPathInfo()).thenReturn("/a/b/c.js");
        Assert.assertTrue(resourceCacheControl.isCacheableResourceRequest(servletRequest));
    }
}
