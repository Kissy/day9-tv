package tv.day9.apk.config;

/**
 * - .
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: DownloadConstants.java 126 2011-11-28 07:41:59Z Kissy $
 */
public class DownloadConstants {

    /**
     * This download hasn't stated yet
     */
    public static final int STATUS_PENDING = 190;

    /**
     * This download has started
     */
    public static final int STATUS_RUNNING = 192;

    /**
     * This download has successfully completed.
     * Warning: there might be other status values that indicate success
     * in the future.
     */
    public static final int STATUS_SUCCESS = 200;

    /**
     * This download was canceled
     */
    public static final int STATUS_CANCELED = 490;

}
