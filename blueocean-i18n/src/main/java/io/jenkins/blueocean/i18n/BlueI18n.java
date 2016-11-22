/*
 * The MIT License
 *
 * Copyright (c) 2015, CloudBees, Inc.
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
package io.jenkins.blueocean.i18n;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.util.HttpResponses;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Internationalization REST (ish) API for Blue Ocean.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
@Restricted(NoExternalUse.class)
public class BlueI18n implements RootAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconFileName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrlName() {
        return "blueocean-i18n";
    }

    /**
     * Get a localised resource bundle.
     * <p>
     * URL: {@code blueocean-i18n/$PLUGIN_NAME/$PLUGIN_VERSION/$BUNDLE_NAME/$LOCALE} (where {@code $LOCALE} is optional).
     *
     * @param request The request.
     * @return The JSON response.
     */
    public HttpResponse doDynamic(StaplerRequest request) {
        BundleParams bundleParams = getBundleParameters(request.getRestOfPath());

        if (bundleParams == null) {
            return HttpResponses.errorJSON("All mandatory bundle identification parameters not specified: '$PLUGIN_NAME/$PLUGIN_VERSION/$BUNDLE_NAME' (and optional $LOCALE).");
        }

        try {
            Locale locale = bundleParams.getLocale();

            if (locale == null) {
                locale = request.getLocale();
            }

            return HttpResponses.okJSON(getBundle(bundleParams, locale));
        } catch (Exception e) {
            return HttpResponses.errorJSON(e.getMessage());
        }
    }

    private JSONObject getBundle(BundleParams bundleParams, Locale locale) {
        return null;
    }

    static BundleParams getBundleParameters(String restOfPath) {
        if (restOfPath == null || restOfPath.length() == 0) {
            return null;
        }

        String[] pathTokens = restOfPath.split("/");
        List<String> bundleParameters = new ArrayList<>();

        for (String pathToken : pathTokens) {
            if (pathToken.length() > 0) {
                bundleParameters.add(pathToken);
            }
        }

        if (bundleParameters.size() != 3 && bundleParameters.size() != 4) {
            return null;
        }

        BundleParams bundleParams = new BundleParams();
        bundleParams.pluginName = bundleParameters.get(0);
        bundleParams.pluginVersion = bundleParameters.get(1);
        bundleParams.bundleName = bundleParameters.get(2);

        if (bundleParameters.size() == 4) {
            // https://www.w3.org/International/questions/qa-lang-priorities
            // in case we have regions/countries in the language query parameter
            String locale = bundleParameters.get(3);
            String[] localeTokens = locale.split("-|_");
            bundleParams.language = localeTokens[0];
            if (localeTokens.length > 1) {
                bundleParams.country = localeTokens[1];
                if (localeTokens.length > 2) {
                    bundleParams.variant = localeTokens[2];
                }
            }
        }

        return bundleParams;
    }

    static class BundleParams {
        String pluginName;
        String pluginVersion;
        String bundleName;
        String language;
        String country;
        String variant;

        Locale getLocale() {
            if (language != null && country != null && variant != null) {
                return new Locale(language, country, variant);
            } else if (language != null && country != null) {
                return new Locale(language, country);
            } else if (language != null) {
                return new Locale(language);
            } else {
                return null;
            }
        }

        // We declare the plugin to be a release version iff
        // the version string contains only numerics.
        private static Pattern RELEASE_VERSION_PATTERN = Pattern.compile("[\\d/.]{3,}");
        boolean isReleaseVersion() {
            return RELEASE_VERSION_PATTERN.matcher(pluginVersion).matches();
        }
    }
}
