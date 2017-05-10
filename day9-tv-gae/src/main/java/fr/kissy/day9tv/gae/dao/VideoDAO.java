package fr.kissy.day9tv.gae.dao;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;
import com.googlecode.objectify.impl.QueryImpl;
import fr.kissy.day9tv.gae.model.Video;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Video DAO.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id$
 */
public class VideoDAO {
    public static final int NUMBER_RETURNED_VIDEOS = 10;

    private static VideoDAO instance;

    static {
        ObjectifyService.register(Video.class);
    }

    /**
     * Save or update the video list.
     *
     * @param videos the video list to save or update.
     */
    public void saveOrUpdate(List<Video> videos) {
        ofy().save().entities(videos).now();
    }

    /**
     * Get the video list from a starting cursor.
     * limit the number of results returned by <code>NUMBER_RETURNED_VIDEOS</code>.
     *
     * @param cursor The cursor to start from. Can be null.
     * @return The videos iterator.
     */
    public QueryResultIterator<Video> getBefore(String cursor) {
        return prepareQueryFromCursor(cursor).order("-timestamp").iterator();
    }

    /**
     * Get the video list from a starting cursor.
     * limit the number of results returned by <code>NUMBER_RETURNED_VIDEOS</code>.
     *
     * @param cursor The cursor to start from. Can be null.
     * @return The videos iterator.
     */
    public QueryResultIterator<Video> getAfter(String cursor) {
        return prepareQueryFromCursor(cursor).iterator();
    }

    /**
     * Prepare a Query from the given cursor. The cursor can be null.
     *
     * @param cursor The video cursor to start from.
     * @return The created Query.
     */
    private Query<Video> prepareQueryFromCursor(String cursor) {
        Query<Video> query = ofy().load().type(Video.class).limit(NUMBER_RETURNED_VIDEOS);
        if (StringUtils.isNotBlank(cursor)) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        return query;
    }

    /**
     * Get the video dao instance.
     *
     * @return The instance.
     */
    public static VideoDAO getInstance() {
        if (instance == null) {
            instance = new VideoDAO();
        }
        return instance;
    }
}
