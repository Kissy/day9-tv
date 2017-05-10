package fr.kissy.day9tv.gae.servlet.admin;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import fr.kissy.day9tv.gae.dao.DeviceDAO;
import fr.kissy.day9tv.gae.model.Device;
import fr.kissy.day9tv.gae.service.C2dmService;
import fr.kissy.day9tv.gae.servlet.CronServlet;
import fr.kissy.day9tv.gae.servlet.worker.NotificationsServlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

/**
 * Cron servlet.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: NotificationsTaskServlet.java 180 2012-01-16 17:32:49Z kissy $
 */
public class NotificationsTaskServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CronServlet.class.getName());

    private DeviceDAO deviceDAO = DeviceDAO.getInstance();

    /**
     * @inheritDoc
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String deviceId = request.getParameter("device.id");
        if (deviceId != null && !deviceId.equals("")) {
            if ("ALL".equalsIgnoreCase(deviceId)) {
                // Notify all devices.
                Queue queue = QueueFactory.getQueue(NotificationsServlet.WORKER_QUEUE_NAME);
                queue.add(withUrl(NotificationsServlet.WORKER_QUEUE_URL).method(TaskOptions.Method.GET));
            } else {
                // Else notify only one
                Device device = deviceDAO.getDeviceWithId(deviceId);
                if (device != null && device.getDeviceRegistrationId() != null) {
                    C2dmService c2dmService = C2dmService.get(getServletContext());

                    Map<String, String[]> params = new HashMap<String, String[]>();
                    params.put("data.message", new String[]{"sync"});

                    String email = device.getAccountName();
                    String collapseKey = Long.toHexString(email.hashCode());
                    try {
                        c2dmService.sendNoRetry(device.getDeviceRegistrationId(), collapseKey, params, true);
                    } catch (IOException e) {
                        LOGGER.severe("Impossible to send C2DM message to device " + device.getId() + " : " + e);
                    }
                }
            }
        }

        response.sendRedirect("/admin/index.jsp");
    }
}
