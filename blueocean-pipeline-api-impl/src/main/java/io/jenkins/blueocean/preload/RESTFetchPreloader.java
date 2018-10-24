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
package io.jenkins.blueocean.preload;

import io.jenkins.blueocean.commons.PageStatePreloader;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;

/**
 * REST prefetch data preloader.
 * <p>
 * Pre-populates the page with REST data, allowing the client side {@code Fetch}
 * module (see {@code Fetch} module in the {@code @jenkins-cd/blueocean-core-js NPM packages})
 * to avoid the REST API call overhead.
 * <p>
 * Create implementations of this class (and annotate with {@code @Extension}) for data that
 * we know is going to be needed by the page.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Restricted(NoExternalUse.class)
public abstract class RESTFetchPreloader extends PageStatePreloader
{

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getStatePropertyPath() {
        return "prefetchdata." + getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getStateJson() {
        BlueUrlTokenizer blueUrl = BlueUrlTokenizer.parseCurrentRequest();

        if (blueUrl == null) {
            // Not a Blue Ocean page, so nothing to be added.
            return null;
        }

        FetchData fetchData = getFetchData(blueUrl);
        if (fetchData != null) {
            return fetchData.toJSON();
        }
        return null;
    }

    protected abstract FetchData getFetchData(@Nonnull BlueUrlTokenizer blueUrl);

    public static final class FetchData {

        private String restUrl;
        private String data;

        public FetchData(@Nonnull String restUrl, @Nonnull String data) {
            this.restUrl = restUrl;
            this.data = data;
        }

        public String getRestUrl() {
            return restUrl;
        }

        public String getData() {
            return data;
        }

        public String toJSON() {
            JSONObject json = new JSONObject();
            json.put("restUrl", restUrl);
            json.put("data", data);
            return json.toString();
        }
    }
}
