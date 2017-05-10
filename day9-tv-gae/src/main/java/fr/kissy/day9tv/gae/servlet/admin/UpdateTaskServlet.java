package fr.kissy.day9tv.gae.servlet.admin;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import fr.kissy.day9tv.gae.dao.LastVideoDAO;
import fr.kissy.day9tv.gae.model.LastVideo;
import fr.kissy.day9tv.gae.servlet.CronServlet;
import fr.kissy.day9tv.gae.servlet.worker.UpdateServlet;

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
 * @version $Id: UpdateTaskServlet.java 223 2013-04-22 23:15:38Z Kissy $
 */
public class UpdateTaskServlet extends HttpServlet {
    /**
     * @inheritDoc
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Schedule Task Queue
        String page = request.getParameter("taskQueue.page");
        Queue queue = QueueFactory.getQueue(UpdateServlet.WORKER_QUEUE_NAME);
        queue.add(withUrl(UpdateServlet.WORKER_QUEUE_URL).param(UpdateServlet.WORKER_PAGE_PARAM, page).method(TaskOptions.Method.GET));
        response.sendRedirect("/admin/index.jsp");
    }
}
