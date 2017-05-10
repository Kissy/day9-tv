package fr.kissy.day9tv.gae.servlet.admin;

import fr.kissy.day9tv.gae.dao.LastVideoDAO;
import fr.kissy.day9tv.gae.model.LastVideo;
import fr.kissy.day9tv.gae.servlet.CronServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Cron servlet.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: UpdateTaskServlet.java 223 2013-04-22 23:15:38Z Kissy $
 */
public class EditLastVideoServlet extends HttpServlet {
    private LastVideoDAO lastVideoDAO = LastVideoDAO.getInstance();

    /**
     * @inheritDoc
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Change latest video ID
        LastVideo lastVideo = lastVideoDAO.get();
        lastVideo.setTimestamp(Long.parseLong(request.getParameter("lastVideo.timestamp")));
        lastVideoDAO.saveOrUpdate(lastVideo);
        response.sendRedirect("/admin/index.jsp");
    }
}
