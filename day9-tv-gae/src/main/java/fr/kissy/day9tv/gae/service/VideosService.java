package fr.kissy.day9tv.gae.service;

import com.google.appengine.api.datastore.QueryResultIterator;
import fr.kissy.day9tv.gae.dao.VideoDAO;
import fr.kissy.day9tv.gae.model.Video;
import fr.kissy.day9tv.gae.proto.VideosProto;

/**
 * Video service.
 * Retrieve the list of video service paginated.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: VideosService.java 223 2013-04-22 23:15:38Z Kissy $
 */
public class VideosService {
    private static VideosService singleton;

    private final VideoDAO videoDAO = VideoDAO.getInstance();

    /**
     * Get the video list starting from a cursor.
     * The results are limited to <code>VideoDAO.NUMBER_RETURNED_VIDEOS</code>.
     *
     * @param cursor The cursor to start the list from.
     * @return The list of video from a cursor.
     */
    public VideosProto.Videos.Builder getVideosProtoBuilder(String cursor) {
        QueryResultIterator<Video> videos = videoDAO.getBefore(cursor);
        return getBuilderFromIterator(videos);
    }

    /**
     * Get the new video list starting from a cursor.
     * The results are limited to <code>VideoDAO.NUMBER_RETURNED_VIDEOS</code>.
     *
     * @param cursor The cursor to start the list from.
     * @return The list of video from a cursor.
     */
    public VideosProto.Videos.Builder getNewVideosProtoBuilder(String cursor) {
        QueryResultIterator<Video> videos = videoDAO.getAfter(cursor);
        return getBuilderFromIterator(videos);
    }

    /**
     * Convert the video iterator to video proto builder.
     *
     * @param videos The video iterator to convert.
     * @return The created video proto builder.
     */
    private VideosProto.Videos.Builder getBuilderFromIterator(QueryResultIterator<Video> videos) {
        VideosProto.Videos.Builder videosProto = VideosProto.Videos.newBuilder();
        videosProto.setStartCursor(videos.getCursor().toWebSafeString());

        while (videos.hasNext()) {
            videosProto.addVideos(videos.next().toBuilder());
        }

        videosProto.setEndCursror(videos.getCursor().toWebSafeString());
        return videosProto;
    }

    /**
     * Get the singleton.
     *
     * @return The singleton.
     */
    public static VideosService getSingleton() {
        if (singleton == null) {
            singleton = new VideosService();
        }
        return singleton;
    }
}
