package fr.kissy.day9tv.gae.dao;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;
import fr.kissy.day9tv.gae.model.Device;

import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Video DAO.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id$
 */
public class DeviceDAO {
    private static final String ACCOUNT_NAME = "accountName";

    private static DeviceDAO instance;

    static {
        ObjectifyService.register(Device.class);
    }

    /**
     * Delete a device.
     *
     * @param device The device to delete.
     */
    public void delete(Device device) {
        ofy().delete().entity(device).now();
    }

    /**
     * Get the list of devices by accountName from database.
     *
     * @param accountName The accountName.
     * @return The device list.
     */
    public List<Device> getDevicesWithAccountName(String accountName) {
        return ofy().load().type(Device.class).filter(ACCOUNT_NAME, accountName).list();
    }

    /**
     * Get device with id.
     *
     * @param id The device id.
     * @return The found device.
     */
    public Device getDeviceWithId(String id) {
        return ofy().load().type(Device.class).id(id).now();
    }

    /**
     * Save or update the device.
     *
     * @param device The device.
     */
    public void saveOrUpdate(Device device) {
        ofy().save().entity(device).now();
    }

    /**
     * Get a query for Device.
     *
     * @return The query.
     */
    public Query<Device> getQuery() {
        return ofy().load().type(Device.class);
    }

    /**
     * Get the instance.
     *
     * @return The instance.
     */
    public static DeviceDAO getInstance() {
        if (instance == null) {
            instance = new DeviceDAO();
        }
        return instance;
    }
}
