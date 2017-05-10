package fr.kissy.day9tv.gae.handler;

import fr.kissy.day9tv.gae.handler.impl.BlipVideoLinkHandler;
import net.htmlparser.jericho.Element;
import fr.kissy.day9tv.gae.model.VideoPart;

import java.net.URL;

/**
 * Video Link handler.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id$
 */
public abstract class VideoLinkHandler {
    private static final BlipVideoLinkHandler BLIP_VIDEO_LINK_HANDLER = new BlipVideoLinkHandler();

    /**
     * Handle the link.
     * Check youtube implementation or blip implementation.
     *
     * @param url The link to handle.
     * @return The VideoPart.
     */
     public abstract VideoPart handleLink(URL url);

    /**
     * Get the video link handler from url.
     *
     * @param url The url.
     * @return The video link handler.
     */
    public static VideoLinkHandler newFrom(URL url) {
        if (url.getHost().contains("blip")) {
            return BLIP_VIDEO_LINK_HANDLER;
        } else {
            return null;
        }
    }
}
