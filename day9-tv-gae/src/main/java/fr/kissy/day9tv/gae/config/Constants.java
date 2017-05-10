package fr.kissy.day9tv.gae.config;

/**
 * Constants.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: Constants.java 223 2013-04-22 23:15:38Z Kissy $
 */
public class Constants {
    public static final String APPENGINE_QUEUE_NAME = "X-AppEngine-QueueName";
    public static final String APPENGINE_RETRY_COUNT = "X-AppEngine-TaskRetryCount";
    public static final String BASE_URL = "http://day9.tv";
    //public static final String VIDEO_LIST_URL = "http://blip.tv/day9tv?skin=api&page=";
    public static final String VIDEO_LIST_URL = BASE_URL + "/archives/?page=";

    // VIDEO XML TAGS
    public static final String TAG_ASSETS = "asset";
    public static final String TAG_ITEM_ID = "item_id";
    public static final String TAG_MEDIA = "media";
    public static final String TAG_MEDIA_ROLE = "role";
    public static final String TAG_MEDIA_LINK = "link";
    public static final String TAG_MEDIA_WIDTH = "width";
    public static final String TAG_MEDIA_HEIGHT = "height";
    public static final String TAG_MEDIA_DURATION = "duration";
    public static final String TAG_MEDIA_SIZE = "size";
}
