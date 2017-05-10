package fr.kissy.day9tv.gae.service;

import fr.kissy.day9tv.gae.config.Constants;
import fr.kissy.day9tv.gae.dao.C2dmDAO;
import net.htmlparser.jericho.Source;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Source service.
 * Get the Source object from an URL.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id$
 */
public class SourceService {
    private static final Logger LOGGER = Logger.getLogger(SourceService.class.getName());
    private static SourceService singleton;

    /**
     * Get the Source document from an URL.
     *
     * @param url The URL to get the source from.
     * @return The Source object or null if something wrong happened.
     */
    public Source get(String url) {
        try {
            return new Source(new URL(url));
        } catch (IOException e) {
            LOGGER.severe("Cannot fetch the HTML document from " + url + ". Error is " + e.getMessage());
            return null;
        }
    }

    public synchronized static SourceService getInstance() {
        if (singleton == null) {
            singleton = new SourceService();
        }
        return singleton;
    }
}
