package fr.kissy.day9tv.gae.servlet.worker;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.cmd.Query;
import fr.kissy.day9tv.gae.config.Constants;
import fr.kissy.day9tv.gae.dao.DeviceDAO;
import fr.kissy.day9tv.gae.model.Device;
import fr.kissy.day9tv.gae.service.C2dmService;

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
 * @version $Id: NotificationsServlet.java 159 2012-01-03 22:47:04Z Kissy $
 */
public class NotificationsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(NotificationsServlet.class.getName());

    public static final String WORKER_CURSOR_PARAM = "cursor";
    public static final String WORKER_QUEUE_NAME = "notifications";
    public static final String WORKER_QUEUE_URL = "/tasks/worker/notifications";

    private static final String DATA = "data.";
    private static final String MESSAGE_EXTRA = "message";
    private static final String MESSAGE_SYNC = "sync";
    
    private static final int LIMIT_MILLIS = 1000 * 25;
    
    private DeviceDAO deviceDAO = DeviceDAO.getInstance();

    private static final String INVALID_REGISTRATION  = "InvalidRegistration";
    private static final String MISMATCH_SENDER_ID  = "MismatchSenderId";
    private static final String NOT_REGISTERED = "NotRegistered";

    /**
     * @inheritDoc
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final long startTime = System.currentTimeMillis();

        // Check that it's a valid Task Queue job
        if (!WORKER_QUEUE_NAME.equals(request.getHeader(Constants.APPENGINE_QUEUE_NAME))) {
            LOGGER.severe("Trying to call -Worker- Task Queue without correct headers");
            return;
        }

        Query<Device> query = deviceDAO.getQuery();
        String cursorStr = request.getParameter(WORKER_CURSOR_PARAM);
        if (cursorStr != null) {
            query.startAt(Cursor.fromWebSafeString(cursorStr));
        }

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(DATA + MESSAGE_EXTRA, new String[]{MESSAGE_SYNC});

        int messageSent = 0;
        ServletContext context = getServletContext();
        C2dmService c2dmService = C2dmService.get(context);
        QueryResultIterator<Device> iterator = query.iterator();
        while (iterator.hasNext()) {
            // Do not spend more than 30s
            if (System.currentTimeMillis() - startTime > LIMIT_MILLIS) {
                Cursor cursor = iterator.getCursor();
                Queue queue = QueueFactory.getQueue(WORKER_QUEUE_NAME);
                queue.add(withUrl(WORKER_QUEUE_URL).param(WORKER_CURSOR_PARAM, cursor.toWebSafeString()).method(TaskOptions.Method.GET));
                break;
            }

            // Process next
            Device device = iterator.next();
            if (device.getDeviceRegistrationId() == null) {
                continue;
            }

            String email = device.getAccountName();
            String collapseKey = Long.toHexString(email.hashCode());

            try {
                if (c2dmService.sendNoRetry(device.getDeviceRegistrationId(), collapseKey, params, true)) {
                    messageSent++;
                } else {
                    // If return false, there is an error.
                    throw new IOException();
                }
            } catch (IOException e) {
                LOGGER.severe("Impossible to send C2DM message to device " + device.getId() + " : " + e);
                if (NOT_REGISTERED.equals(e.getMessage()) || INVALID_REGISTRATION.equals(e.getMessage()) || MISMATCH_SENDER_ID.equals(e.getMessage())) {
                    // Something is wrong with this device, unregister it.
                    deviceDAO.delete(device);
                    LOGGER.warning("Device " + device.getId() + " is no longer valid and has been deleted");
                }
            }
        }

        if (messageSent > 0) {
            LOGGER.info("Success fully sent " + messageSent + " C2DM messages");
        } else {
            LOGGER.info("No device to send C2DM messages to");
        }
    }
}
