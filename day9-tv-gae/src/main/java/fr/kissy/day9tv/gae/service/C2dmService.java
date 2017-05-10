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

package fr.kissy.day9tv.gae.service;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import fr.kissy.day9tv.gae.dao.C2dmDAO;
import fr.kissy.day9tv.gae.servlet.worker.NotificationsRetryServlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Methods to send messages using Android Cloud To Device Messaging.
 */
@SuppressWarnings("serial")
public class C2dmService {
    private static final Logger LOGGER = Logger.getLogger(C2dmService.class.getName());
    private static final String UPDATE_CLIENT_AUTH = "UpdateServlet-Client-Auth";
    public static final String PARAM_REGISTRATION_ID = "registration_id";
    public static final String PARAM_DELAY_WHILE_IDLE = "delay_while_idle";
    public static final String PARAM_COLLAPSE_KEY = "collapse_key";
    private static final String UTF8 = "UTF-8";

    /**
     * Jitter - random interval to wait before retry.
     */
    public static final int DATAMESSAGING_MAX_JITTER_MSEC = 3000;

    private static C2dmService singleton;
    private final C2dmDAO serverConfig;

    // Testing
    protected C2dmService() {
        serverConfig = null;
    }

    private C2dmService(C2dmDAO serverConfig) {
        this.serverConfig = serverConfig;
    }

    public boolean sendNoRetry(String registrationId, String collapse, Map<String, String[]> params,
                               boolean delayWhileIdle) throws IOException {
        // Send a sync message to this Android device.
        StringBuilder postDataBuilder = new StringBuilder();
        postDataBuilder.append(PARAM_REGISTRATION_ID).append("=").append(registrationId);

        if (delayWhileIdle) {
            postDataBuilder.append("&").append(PARAM_DELAY_WHILE_IDLE).append("=1");
        }
        postDataBuilder.append("&").append(PARAM_COLLAPSE_KEY).append("=").append(collapse);

        for (Object keyObj : params.keySet()) {
            String key = (String) keyObj;
            if (key.startsWith("data.")) {
                String[] values = params.get(key);
                postDataBuilder.append("&").append(key).append("=").append(
                        URLEncoder.encode(values[0], UTF8));
            }
        }

        byte[] postData = postDataBuilder.toString().getBytes(UTF8);

        // Hit the dm URL.
        URL url = new URL(serverConfig.getC2DMUrl());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        // In the App Engine 1.4.3 SDK, setting the content length can trigger an exception
        // conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
        String authToken = serverConfig.getToken();
        conn.setRequestProperty("Authorization", "GoogleLogin auth=" + authToken);

        OutputStream out = conn.getOutputStream();
        out.write(postData);
        out.close();

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpServletResponse.SC_UNAUTHORIZED
                || responseCode == HttpServletResponse.SC_FORBIDDEN) {
            // The token is too old - return false to retry later, will fetch the
            // token from DB. This happens if the password is changed or token expires.
            // Either admin is updating the token, or UpdateServlet-Client-Auth was received by another
            // server, and next retry will get the good one from database.
            LOGGER.warning("Unauthorized - need token");
            serverConfig.invalidateCachedToken();
            return false;
        }

        // Check for updated token header
        String updatedAuthToken = conn.getHeaderField(UPDATE_CLIENT_AUTH);
        if (updatedAuthToken != null && !authToken.equals(updatedAuthToken)) {
            LOGGER.info("Got updated auth token from datamessaging servers: " + updatedAuthToken);
            serverConfig.updateToken(updatedAuthToken);
        }

        String responseLine =
                new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();

        // NOTE: You *MUST* use exponential backoff if you receive a 503 response
        // code. Since App Engine's task queue mechanism automatically does this for tasks
        // that return non-success error codes, this is not explicitly implemented here.
        // If we weren't using App Engine, we'd need to manually implement this.
        if (responseLine == null || responseLine.equals("")) {
            LOGGER.info("Got " + responseCode + " response from Google AC2DM endpoint.");
            throw new IOException("Got empty response from Google AC2DM endpoint.");
        }

        String[] responseParts = responseLine.split("=", 2);
        if (responseParts.length != 2) {
            LOGGER.warning("Invalid message from google: " + responseCode + " " + responseLine);
            throw new IOException("Invalid response from Google " + responseCode + " " + responseLine);
        }

        if (responseParts[0].equals("id")) {
            LOGGER.fine("Successfully sent data message to device: " + responseLine);
            return true;
        }

        if (responseParts[0].equals("Error")) {
            String err = responseParts[1];
            LOGGER.warning("Got error response from Google datamessaging endpoint: " + err);
            // No retry.
            throw new IOException(err);
        } else {
            // 500 or unparseable response - server error, needs to retry
            LOGGER.warning("Invalid response from google " + responseLine + " " + responseCode);
            return false;
        }
    }

    public void sendWithRetry(String registrationId, String collapse, Map<String, String[]> params,
                              boolean delayWhileIdle) throws IOException {

        boolean sentOk = sendNoRetry(registrationId, collapse, params, delayWhileIdle);
        if (!sentOk) {
            retry(registrationId, collapse, params, delayWhileIdle);
        }
    }

    private void retry(String token, String collapseKey, Map<String, String[]> params,
                       boolean delayWhileIdle) {
        Queue dmQueue = QueueFactory.getQueue(NotificationsRetryServlet.WORKER_QUEUE_NAME);
        try {
            TaskOptions url =
                    TaskOptions.Builder.withUrl(NotificationsRetryServlet.WORKER_QUEUE_URL).param(
                            C2dmService.PARAM_REGISTRATION_ID, token).param(C2dmService.PARAM_COLLAPSE_KEY,
                            collapseKey);
            if (delayWhileIdle) {
                url.param(PARAM_DELAY_WHILE_IDLE, "1");
            }
            for (String key : params.keySet()) {
                String[] values = params.get(key);
                url.param(key, URLEncoder.encode(values[0], UTF8));
            }

            // Task queue implements the exponential backoff
            long jitter = (int) Math.random() * DATAMESSAGING_MAX_JITTER_MSEC;
            url.countdownMillis(jitter);

            dmQueue.add(url);
        } catch (UnsupportedEncodingException e) {
            // Ignore - UTF8 should be supported
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
    }

    public synchronized static C2dmService get(ServletContext servletContext) {
        if (singleton == null) {
            C2dmDAO serverConfig = C2dmDAO.get(servletContext);
            singleton = new C2dmService(serverConfig);
        }
        return singleton;
    }

}
