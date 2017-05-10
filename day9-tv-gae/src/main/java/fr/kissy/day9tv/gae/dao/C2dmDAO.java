/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package fr.kissy.day9tv.gae.dao;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.ObjectifyService;
import fr.kissy.day9tv.gae.model.C2dm;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Stores config information related to data messaging.
 */
public class C2dmDAO {
    private static final Logger LOGGER = Logger.getLogger(C2dmDAO.class.getName());
    private static final String C2DM_TOKEN_PATH = "/WEB-INF/c2dm-token.txt";
    private static final Long CONFIG_ID = 1L;

    private static C2dmDAO instance;

    private ServletContext servletContext;
    private String currentToken;
    private String c2dmUrl;

    static {
        ObjectifyService.register(C2dm.class);
    }

    private C2dmDAO(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * UpdateServlet the token.
     * <p/>
     * Called on "UpdateServlet-Client-Auth" or when admins set a new token.
     *
     * @param token The token.
     */
    public void updateToken(String token) {
        if (token != null) {
            currentToken = token;
            C2dm config = getC2dmConfig();
            config.setAuthToken(currentToken);
            ofy().save().entity(config).now();
        }
    }

    /**
     * Token expired
     */
    public void invalidateCachedToken() {
        currentToken = null;
    }

    /**
     * Return the auth token from the database. Should be called only if the old
     * token expired.
     *
     * @return The token.
     */
    public String getToken() {
        if (currentToken == null) {
            currentToken = getC2dmConfig().getAuthToken();
        }
        return currentToken;
    }

    public String getC2DMUrl() {
        if (c2dmUrl == null) {
            c2dmUrl = getC2dmConfig().getC2DMUrl();
        }
        return c2dmUrl;
    }

    private C2dm getC2dmConfig() {
        C2dm dmConfig = ofy().load().key(Key.create(C2dm.class, CONFIG_ID)).now();
        if (dmConfig == null) {
            // Create a new JDO object
            dmConfig = new C2dm();
            dmConfig.setId(CONFIG_ID);
            try {
                InputStream is = servletContext.getResourceAsStream(C2DM_TOKEN_PATH);
                if (is != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String token = reader.readLine();
                    dmConfig.setAuthToken(token);
                    ofy().save().entity(dmConfig).now();
                } else {
                    LOGGER.warning("Could not locate " + C2DM_TOKEN_PATH);
                }
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Can't load initial token", t);
            }
        }
        return dmConfig;
    }

    public synchronized static C2dmDAO get(ServletContext servletContext) {
        if (instance == null) {
            instance = new C2dmDAO(servletContext);
        }
        return instance;
    }
}
