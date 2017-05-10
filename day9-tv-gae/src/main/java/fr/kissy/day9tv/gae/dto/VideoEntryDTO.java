package fr.kissy.day9tv.gae.dto;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Archive Entry DTO.
 * Result from the ParserService after parsing one archive page.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id$
 */
public class VideoEntryDTO {
    private String title;
    private String detailUrl;
    private Long timestamp;

    public VideoEntryDTO(String title, String detailUrl, Long timestamp) {
        this.title = title;
        this.detailUrl = detailUrl;
        this.timestamp = timestamp;
    }

    public VideoEntryDTO(Element result) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ");
        Element link = result.getFirstElement(HTMLElementName.H3).getFirstElement(HTMLElementName.A);
        title = link.getContent().toString().trim();
        detailUrl = link.getAttributeValue("href");
        timestamp = dateFormat.parse(result.getFirstElement("time").getAttributeValue("datetime")).getTime();
    }

    public String getTitle() {
        return title;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
