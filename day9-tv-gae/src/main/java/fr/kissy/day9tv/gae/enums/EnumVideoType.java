package fr.kissy.day9tv.gae.enums;

import fr.kissy.day9tv.gae.model.Video;

/**
 * Enum video type.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: EnumVideoType.java 223 2013-04-22 23:15:38Z Kissy $
 */
public enum EnumVideoType {
    DAY9_DAILY("Day[9] Daily #"),
    METADATING("MetaDating #"),

    // Real other
    OTHER(null);

    private String titleMatch;

    /**
     * Default constructor.
     * 
     * @param titleMatch The string to match title.
     */
    private EnumVideoType(String titleMatch) {
        this.titleMatch = titleMatch;
    }

    /**
     * Get the enum video type from video.
     *
     *
     * @param video The video video.
     * @return The enum video type.
     */
    public static EnumVideoType fromVideo(Video video) {
        for (EnumVideoType type : EnumVideoType.values()) {
            if (type == OTHER) {
                continue;
            }
            if (video.getTitle().toLowerCase().startsWith(type.titleMatch.toLowerCase())) {
                return type;
            }
        }

        return OTHER;
    }
}
