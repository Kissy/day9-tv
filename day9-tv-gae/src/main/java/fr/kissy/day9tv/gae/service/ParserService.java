package fr.kissy.day9tv.gae.service;

import com.google.common.collect.Lists;
import fr.kissy.day9tv.gae.config.Constants;
import fr.kissy.day9tv.gae.dto.VideoEntryDTO;
import fr.kissy.day9tv.gae.enums.EnumVideoSubType;
import fr.kissy.day9tv.gae.enums.EnumVideoType;
import fr.kissy.day9tv.gae.handler.VideoLinkHandler;
import fr.kissy.day9tv.gae.model.Video;
import fr.kissy.day9tv.gae.model.VideoPart;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Parser service.
 * Parse the videos from the HTML pages.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id$
 */
public class ParserService {
    private static final Logger LOGGER = Logger.getLogger(ParserService.class.getName());
    private static ParserService singleton;

    private SourceService sourceService = SourceService.getInstance();

    /**
     * Parse the newest archives from the website.
     *
     * @param page The archive page to parse.
     * @param timestamp The last parsed archive entry timestamp.
     * @return The list of newest archive entries.
     */
    public List<VideoEntryDTO> parseNewerVideos(Integer page, Long timestamp) {
        List<VideoEntryDTO> results = Lists.newArrayList();
        Source archiveDocument = sourceService.get(Constants.VIDEO_LIST_URL + page);
        if (archiveDocument == null) {
            return null;
        }

        Element resultsContainer = archiveDocument.getElementById("results");
        List<Element> resultsElements = resultsContainer.getChildElements();
        Collections.reverse(resultsElements);

        for (Element result : resultsElements) {
            try {
                VideoEntryDTO videoEntryDTO = new VideoEntryDTO(result);
                if (videoEntryDTO.getTimestamp() > timestamp) {
                    results.add(videoEntryDTO);
                }
            } catch (ParseException e) {
                LOGGER.severe("Error while parsing element archive HTML element. Error is " + e.getMessage());
            }
        }

        LOGGER.info("Fetched archives from " + Constants.VIDEO_LIST_URL + page + ", found " + results.size() + " entries.");
        return results;
    }

    /**
     * Parse one video entry from an Archive entry.
     *
     * @param videoEntryDTO The VideoEntryDTO to fetch the video entry from.
     * @return The Video created from the VideoEntryDTO.
     */
    public Video parseVideoEntry(VideoEntryDTO videoEntryDTO) {
        Source detailDocument = sourceService.get(Constants.BASE_URL + videoEntryDTO.getDetailUrl());
        Element contentElement = detailDocument.getElementById("content");

        Video video = new Video(videoEntryDTO);

        // Description
        boolean inDescription = false;
        StringBuilder description = new StringBuilder();
        for (Element element : contentElement.getChildElements()) {
            if (HTMLElementName.H2.equals(element.getName())) {
                String content = element.getContent().toString();
                inDescription = content.equalsIgnoreCase("description");
                if (!inDescription) {
                    break;
                }
            }
            if (inDescription && HTMLElementName.P.equals(element.getName())) {
                description.append(element.getContent().getTextExtractor().toString()).append("\n");
            }
        }
        video.setDescription(description.toString());

        // Tags
        Element tagsElement = detailDocument.getElementById("tags");
        for (Element element : tagsElement.getAllElements(HTMLElementName.A)) {
            video.getTags().add(element.getContent().toString());
        }

        // Video links
        for (Element element : contentElement.getAllElements(HTMLElementName.A)) {
            if (!element.getAttributeValue("target").equals("_blank")) {
                continue;
            }

            try {
                URL url = new URL(element.getAttributeValue("href") + "?skin=api");
                VideoLinkHandler videoLinkHandler = VideoLinkHandler.newFrom(url);
                if (videoLinkHandler == null) {
                    LOGGER.warning("Cannot find a link handler for the URL " + url);
                    continue;
                }

                VideoPart videoPart = videoLinkHandler.handleLink(url);
                String part = element.getTextExtractor().toString().replace("Part ", "");
                videoPart.setPart(Integer.valueOf(part));
                video.getVideoParts().add(videoPart);
            } catch (MalformedURLException e) {
                LOGGER.severe("Cannot parse the URL " + element.getAttributeValue("href") + ". Error is " + e.getMessage());
            }
        }

        LOGGER.info("Fetched video entry for video " + video.getTitle());
        return video;
    }

    public void parseTypeAndSubType(Video video) {
        video.setType(EnumVideoType.fromVideo(video));
        video.setSubType(EnumVideoSubType.fromVideo(video));
    }

    public synchronized static ParserService getInstance() {
        if (singleton == null) {
            singleton = new ParserService();
        }
        return singleton;
    }
}
