package fr.kissy.day9tv.gae.handler.impl;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import fr.kissy.day9tv.gae.handler.VideoLinkHandler;
import fr.kissy.day9tv.gae.handler.VideoListHandler;
import fr.kissy.day9tv.gae.model.VideoPart;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Blip Video Link handler.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id$
 */
public class BlipVideoLinkHandler extends VideoLinkHandler {
    private static final Logger LOGGER = Logger.getLogger(BlipVideoLinkHandler.class.getName());

    /**
     * @inheritDoc
     */
    @Override
    public VideoPart handleLink(URL url) {
        VideoListHandler videoListHandler = new VideoListHandler();

        try {
            InputStream inputStream = url.openStream();

            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();

            InputSource inputSource = new InputSource(inputStream);
            xmlReader.setContentHandler(videoListHandler);
            try {
                xmlReader.parse(inputSource);
            } catch (SAXException e) {
                LOGGER.info("SAXException : " + e.getMessage());
            }

            inputStream.close();
        } catch (Exception e) {
            LOGGER.severe("Cannot parse video list from " + url + ". Error is " + e.getMessage());
            return null;
        }

        return videoListHandler.getCurrentVideoPart();
    }
}
