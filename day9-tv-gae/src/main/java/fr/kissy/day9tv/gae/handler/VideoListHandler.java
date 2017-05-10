package fr.kissy.day9tv.gae.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import fr.kissy.day9tv.gae.config.Constants;
import fr.kissy.day9tv.gae.enums.EnumVideoMediaType;
import fr.kissy.day9tv.gae.model.VideoFile;
import fr.kissy.day9tv.gae.model.VideoPart;

import java.util.logging.Logger;

/**
 * Video List parser.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: VideoListHandler.java 223 2013-04-22 23:15:38Z Kissy $
 */
public class VideoListHandler extends DefaultHandler {
    private static final Logger LOGGER = Logger.getLogger(VideoListHandler.class.getName());

    private StringBuilder stringBuilder;
    private VideoPart currentVideoPart = null;
    private VideoFile currentVideoFile = null;

    public VideoListHandler() {
        stringBuilder = new StringBuilder();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attributes) throws SAXException {
        stringBuilder.setLength(0);
        if (qName.equals(Constants.TAG_ASSETS)) {
            currentVideoPart = new VideoPart();
        } else if (qName.equals(Constants.TAG_MEDIA)) {
            currentVideoFile = new VideoFile();
        } else  if (qName.equals(Constants.TAG_MEDIA_LINK)) {
            if (currentVideoFile != null) {
                String file = attributes.getValue(1);
                currentVideoFile.setFile(file.replace("http://blip.tv/file/get/", ""));
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void endElement(final String namespaceURI, final String localName, final String qName) throws SAXException {
        if (qName.equals(Constants.TAG_ITEM_ID)) {
            currentVideoPart.setId(Long.valueOf(stringBuilder.toString()));
        } else if (qName.equals(Constants.TAG_MEDIA_ROLE)) {
            if (currentVideoFile != null) {
                currentVideoFile.setType(EnumVideoMediaType.fromName(stringBuilder.toString()));
            }
        } else if (qName.equals(Constants.TAG_MEDIA_WIDTH)) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("0");
            }
            currentVideoFile.setWidth(Integer.valueOf(stringBuilder.toString()));
        } else if (qName.equals(Constants.TAG_MEDIA_HEIGHT)) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("0");
            }
            currentVideoFile.setHeight(Integer.valueOf(stringBuilder.toString()));
        } else if (qName.equals(Constants.TAG_MEDIA_DURATION)) {
            currentVideoFile.setDuration(Integer.valueOf(stringBuilder.toString()));
        } else if (qName.equals(Constants.TAG_MEDIA_SIZE)) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("0");
            }
            currentVideoFile.setSize(Long.valueOf(stringBuilder.toString()));
        } else if (qName.equals(Constants.TAG_MEDIA)) {
            if (currentVideoPart != null) {
                currentVideoPart.getFiles().add(currentVideoFile);
            }
            currentVideoFile = null;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        super.characters(ch, start, length);
        stringBuilder.append(ch, start, length);
    }

    public VideoPart getCurrentVideoPart() {
        return currentVideoPart;
    }
}
