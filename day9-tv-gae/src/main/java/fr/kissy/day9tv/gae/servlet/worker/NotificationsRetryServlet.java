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

package fr.kissy.day9tv.gae.servlet.worker;

import fr.kissy.day9tv.gae.config.Constants;
import fr.kissy.day9tv.gae.service.C2dmService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A task that sends tickles to device clients. This will be invoked by
 * AppEngine cron to retry failed requests.
 * <p/>
 * You must configure war/WEB-INF/queue.xml and the web.xml entries.
 */
public class NotificationsRetryServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(NotificationsRetryServlet.class.getName());

    public static final String WORKER_QUEUE_NAME = "notifications-retry";
    public static final String WORKER_QUEUE_URL = "/tasks/worker/notifications/retry";

    private static final int MAX_RETRY = 3;

    /**
     * Only admin can make this request.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!WORKER_QUEUE_NAME.equals(request.getHeader(Constants.APPENGINE_QUEUE_NAME))) {
            LOGGER.severe("Trying to call -Worker- Task Queue without correct headers");
            return;
        }

        String registrationId = request.getParameter(C2dmService.PARAM_REGISTRATION_ID);
        String retryCount = request.getHeader(Constants.APPENGINE_RETRY_COUNT);
        if (retryCount != null) {
            int retryCnt = Integer.parseInt(retryCount);
            if (retryCnt > MAX_RETRY) {
                LOGGER.severe("Too many retries, drop message for :" + registrationId);
                response.setStatus(200);
                return; // will not try again.
            }
        }

        @SuppressWarnings({"unchecked", "cast"})
        Map<String, String[]> params = (Map<String, String[]>) request.getParameterMap();
        String collapse = request.getParameter(C2dmService.PARAM_COLLAPSE_KEY);
        boolean delayWhenIdle = null != request.getParameter(C2dmService.PARAM_DELAY_WHILE_IDLE);

        try {
            // Send doesn't retry !!
            // We use the queue exponential backoff for retries.
            boolean sentOk = C2dmService.get(getServletContext()).sendNoRetry(registrationId, collapse, params, delayWhenIdle);
            LOGGER.info("Retry result " + sentOk + " " + registrationId);
            if (sentOk) {
                response.setStatus(200);
                response.getOutputStream().write("OK".getBytes());
            } else {
                response.setStatus(500); // retry this task
            }
        } catch (IOException ex) {
            response.setStatus(200);
            response.getOutputStream().write(("Non-retriable error:" + ex.toString()).getBytes());
        }
    }
}
