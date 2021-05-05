/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
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

import hudson.Extension;
import hudson.model.User;
import io.jenkins.blueocean.commons.stapler.Export;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.model.BlueFavorite;
import io.jenkins.blueocean.rest.model.BlueFavoriteContainer;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import io.jenkins.blueocean.service.embedded.rest.UserImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;

import com.google.common.collect.Iterables;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Preload user favorites.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
public class FavoritesStatePreloader extends RESTFetchPreloader {

    private static final Logger LOGGER = Logger.getLogger(FavoritesStatePreloader.class.getName());

    private static final int DEFAULT_LIMIT = 26;

    @Override
    protected FetchData getFetchData(@Nonnull BlueUrlTokenizer blueUrl) {
        User jenkinsUser = User.current();
        if (jenkinsUser != null) {
            BlueOrganization organization = Iterables.getFirst(OrganizationFactory.getInstance().list(), null);
            if (organization != null) {
                String pipelineFullName = blueUrl.getPart(BlueUrlTokenizer.UrlPart.PIPELINE);

                // don't need this list when at pipeline pages
                if (pipelineFullName != null) {
                    return null;
                }

                UserImpl blueUser = new UserImpl(organization, jenkinsUser, organization.getUsers());
                BlueFavoriteContainer favoritesContainer = blueUser.getFavorites();

                if (favoritesContainer != null) {
                    JSONArray favorites = new JSONArray();
                    // Limit the number of favorites to return to a sane amount
                    Iterator<BlueFavorite> favoritesIterator = favoritesContainer.iterator(0, DEFAULT_LIMIT);

                    while (favoritesIterator.hasNext()) {
                        Reachable favorite = favoritesIterator.next();
                        try {
                            favorites.add(JSONObject.fromObject(Export.toJson(favorite)));
                        } catch (IOException e) {
                            LOGGER.log(Level.FINE, String.format("Unable to preload favorites for User '%s'. Serialization error.", jenkinsUser.getFullName()), e);
                            return null;
                        }
                    }

                    return new FetchData(favoritesContainer.getLink().getHref() + "?start=0&limit=" + DEFAULT_LIMIT, favorites.toString());
                }
            }
        }

        // Don't preload any data on the page.
        return null;
    }
}
