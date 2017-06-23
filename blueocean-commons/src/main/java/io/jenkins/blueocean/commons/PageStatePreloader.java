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
package io.jenkins.blueocean.commons;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Page state "preloader" extension point.
 * <p>
 * Allows the loading page's JavaScript blueocean global scope to
 * be pre-populated with data that we know the page is going to need, thereby
 * providing a mechanism for eliminating the request overhead for that data.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class PageStatePreloader implements ExtensionPoint {

    /**
     * Get the JavaScript object graph path at shiwh the state is to be stored.
     * @return The JavaScript object graph path at shiwh the state is to be stored.
     */
    @Nonnull
    public abstract String getStatePropertyPath();

    /**
     * Get the state JSON to be set in the page's JavaScript blueocean global scope.
     * @return The state JSON to be set in the page's JavaScript blueocean global
     * scope, or {@code null} if no data is to be set of this page.
     */
    @CheckForNull
    public abstract String getStateJson();

    public static ExtensionList<PageStatePreloader> all() {
        return ExtensionList.lookup(PageStatePreloader.class);
    }
}
