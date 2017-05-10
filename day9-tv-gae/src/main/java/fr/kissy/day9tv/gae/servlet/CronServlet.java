package fr.kissy.day9tv.gae.servlet;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import fr.kissy.day9tv.gae.servlet.worker.UpdateServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

/**
 * Cron servlet.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: Cron.java 152 2011-12-28 15:16:50Z kissy $
 */
public class CronServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CronServlet.class.getName());

    /**
     * @inheritDoc
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.info("Starting cron at " + new Date());
        Queue queue = QueueFactory.getQueue(UpdateServlet.WORKER_QUEUE_NAME);
        queue.add(withUrl(UpdateServlet.WORKER_QUEUE_URL).param(UpdateServlet.WORKER_PAGE_PARAM, "1").method(TaskOptions.Method.GET));
    }
}
