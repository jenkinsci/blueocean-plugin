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
package io.jenkins.blueocean.service.embedded;

import hudson.Extension;
import hudson.model.User;
import io.jenkins.blueocean.commons.PageStatePreloader;
import io.jenkins.blueocean.commons.stapler.Export;
import io.jenkins.blueocean.service.embedded.rest.UserImpl;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Preload the user object for the active user.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
public class UserStatePreloader extends PageStatePreloader {

    private static final Logger LOGGER = Logger.getLogger(UserStatePreloader.class.getName());

    private static final String ANONYMOUS;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatePropertyPath() {
        return "user";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStateJson() {
        try {
            User currentUser = User.current();
            if (currentUser != null) {
                return Export.toJson(new UserImpl(currentUser));
            } else {
                return ANONYMOUS;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error serializing active User object and adding to page preload state.");
            return ANONYMOUS;
        }
    }

    static {
        JSONObject anonUserJson = new JSONObject();
        anonUserJson.put("id", "anonymous");
        ANONYMOUS = anonUserJson.toString();
    }
}
