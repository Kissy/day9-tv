package fr.kissy.day9tv.gae.servlet;

import com.googlecode.objectify.NotFoundException;
import fr.kissy.day9tv.gae.dao.DeviceDAO;
import fr.kissy.day9tv.gae.model.Device;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * VideosServlet servlet.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: Register.java 194 2012-01-24 14:53:01Z kissy $
 */
public class RegisterServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CronServlet.class.getName());

    private static final int MAX_DEVICES = 5;
    
    private static final String UTF8 = "utf-8";
    private static final String TEXT_HTML = "text/html";
    private static final String RESPONSE_SUCCESS = "success";
    private static final String REGISTRATION_PARAM = "registration";
    private static final String ACCOUNT_PARAM = "account";
    private static final String DEVICE_PARAM = "device";
    private static final String REGISTER_PARAM = "register";
    private static final String DEVICE_TYPE = "ac2dm";
    
    private DeviceDAO deviceDAO = DeviceDAO.getInstance();

    /**
     * @inheritDoc
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(TEXT_HTML);
        response.setCharacterEncoding(UTF8);

        String deviceRegistrationId;
        String accountName;
        String deviceId;
        boolean register;
        try {
            deviceRegistrationId = request.getParameter(REGISTRATION_PARAM);
            accountName = request.getParameter(ACCOUNT_PARAM);
            deviceId = request.getParameter(DEVICE_PARAM);
            register = Boolean.parseBoolean(request.getParameter(REGISTER_PARAM));
        } catch (Exception e) {
            LOGGER.severe("Exception while trying to parse page parameter.");
            response.getWriter().write(e.toString());
            response.flushBuffer();
            return;
        }

        LOGGER.info("Registering (" + register + ") " + accountName + " for device " + deviceId + " with registration id " + deviceRegistrationId);

        try {
            if (register) {
                doRegister(deviceRegistrationId, DEVICE_TYPE, deviceId, accountName);
            } else {
                doUnregister(deviceRegistrationId, accountName);
            }
        } catch (Exception e) {
            LOGGER.severe("Got exception in registration : " + e + " - " + e.getMessage());
            response.getWriter().write(e.toString());
            response.flushBuffer();
            return;
        }

        response.getWriter().write(RESPONSE_SUCCESS);
        response.flushBuffer();
    }

    /**
     * Do registration.
     *
     * @param deviceRegistrationId The device registration id.
     * @param deviceType The device type.
     * @param deviceId The device id.
     * @param accountName The account name.
     * @throws Exception Exception can occurs.
     */
    private void doRegister(String deviceRegistrationId, String deviceType, String deviceId, String accountName) throws Exception {
        List<Device> devices = deviceDAO.getDevicesWithAccountName(accountName);
        if (devices == null || devices.size() > 0) {
            return;    
        }

        // Handle max devices
        if (devices.size() > MAX_DEVICES) {
            LOGGER.warning("User " + accountName + " already got " + MAX_DEVICES + " devices.");

            Device oldest = devices.get(0);
            if (oldest.getRegistrationTimestamp() == null) {
                deviceDAO.delete(oldest);
            } else {
                long oldestTime = oldest.getRegistrationTimestamp().getTime();
                for (int i = 1; i < devices.size(); i++) {
                    if (devices.get(i).getRegistrationTimestamp().getTime() < oldestTime) {
                        oldest = devices.get(i);
                        oldestTime = oldest.getRegistrationTimestamp().getTime();
                    }
                }
                deviceDAO.delete(oldest);
            }
        }

        // Get device if it already exists, else create
        String id = (deviceId == null ? accountName + "#" + Long.toHexString(Math.abs(deviceRegistrationId.hashCode())) : deviceId);

        Device device;
        try {
            device = deviceDAO.getDeviceWithId(id);

            // update registration id and account name
            device.setDeviceRegistrationId(deviceRegistrationId);
            device.setRegistrationTimestamp(new Date());
            device.setAccountName(accountName);
        } catch (NotFoundException ignored) {
            device = new Device(id, accountName, deviceRegistrationId, deviceType);
        }

        deviceDAO.saveOrUpdate(device);
    }

    /**
     * Do unregistration.
     *
     * @param deviceRegistrationId The device registration id.
     * @param accountName The account name.
     * @throws Exception Exception can occurs.
     */
    private void doUnregister(String deviceRegistrationId, String accountName) throws Exception {
        List<Device> devices = deviceDAO.getDevicesWithAccountName(accountName);
        for (Device device : devices) {
            if (device.getDeviceRegistrationId().equals(deviceRegistrationId)) {
                deviceDAO.delete(device);
                return;
            }
        }

        LOGGER.warning("There is no device to remove from database. Username (" + accountName + ") and device id (" + deviceRegistrationId + ")");
    }
}
