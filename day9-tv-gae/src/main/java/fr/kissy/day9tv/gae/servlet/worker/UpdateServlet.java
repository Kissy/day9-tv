package fr.kissy.day9tv.gae.servlet.worker;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import fr.kissy.day9tv.gae.config.Constants;
import fr.kissy.day9tv.gae.service.UpdaterService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

/**
 * Cron servlet.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: UpdateServlet.java 223 2013-04-22 23:15:38Z Kissy $
 */
public class UpdateServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(UpdateServlet.class.getName());
    
    public static final String WORKER_PAGE_PARAM = "page";
    public static final String WORKER_QUEUE_NAME = "update";
    public static final String WORKER_QUEUE_URL = "/tasks/worker/update";

    private static final int MAX_RETRY = 3;

    private UpdaterService updaterService = UpdaterService.getInstance();

    /**
     * @inheritDoc
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Check that it's a valid Task Queue job
        if (!WORKER_QUEUE_NAME.equals(request.getHeader(Constants.APPENGINE_QUEUE_NAME))) {
            LOGGER.severe("Trying to call -Worker- Task Queue without correct headers");
            return;
        }

        int page = 0;
        if (request.getParameter(WORKER_PAGE_PARAM) != null) {
            page = Integer.valueOf(request.getParameter(WORKER_PAGE_PARAM));
        }

        // Retry count
        String retryCount = request.getHeader(Constants.APPENGINE_RETRY_COUNT);
        if (retryCount != null) {
            int retryCnt = Integer.parseInt(retryCount);
            if (retryCnt > MAX_RETRY) {
                LOGGER.severe("Too many retries, stopped at page " + page);
                response.setStatus(200);
                return;
            }
        }

        boolean success = updaterService.updateVideos(page);

        // Retry if not success
        if (!success) {
            response.setStatus(500);
            return;
        }

        // Go for next page
        if (page > 1) {
            Queue queue = QueueFactory.getQueue(WORKER_QUEUE_NAME);
            queue.add(withUrl(WORKER_QUEUE_URL).param(WORKER_PAGE_PARAM, String.valueOf(page - 1)).method(TaskOptions.Method.GET));
        }

        response.setStatus(200);
    }
}
