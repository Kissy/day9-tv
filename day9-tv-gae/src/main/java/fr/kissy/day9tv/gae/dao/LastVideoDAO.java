package fr.kissy.day9tv.gae.dao;

import com.googlecode.objectify.ObjectifyService;
import fr.kissy.day9tv.gae.model.LastVideo;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Video DAO.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id$
 */
public class LastVideoDAO {
    private static LastVideoDAO instance;

    static {
        ObjectifyService.register(LastVideo.class);
    }

    /**
     * Get the last video from database.
     *
     * @return The last video.
     */
    public LastVideo get() {
        return ofy().load().type(LastVideo.class).id(LastVideo.ID).now();
    }

    /**
     * Save or update the video.
     *
     * @param lastVideo The video to save or update.
     */
    public void saveOrUpdate(LastVideo lastVideo) {
        lastVideo.setId(LastVideo.ID);
        ofy().save().entity(lastVideo).now();
    }

    /**
     * Get the instance.
     *
     * @return The instance.
     */
    public static LastVideoDAO getInstance() {
        if (instance == null) {
            instance = new LastVideoDAO();
        }
        return instance;
    }
}
