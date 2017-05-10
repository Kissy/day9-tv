package fr.kissy.day9tv.gae.enums;

import fr.kissy.day9tv.gae.model.Video;

/**
 * Video sub type.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: EnumVideoSubType.java 223 2013-04-22 23:15:38Z Kissy $
 */
public enum EnumVideoSubType {
    FUNDAY_MONDAY("Funday Monday"),
    NEWBIE_TUESDAY("Newbie Tuesday"),
    NONE(null);

    private String name;

    /**
     * Dedfault constructor.
     *
     * @param name The xml name.
     */
    EnumVideoSubType(String name) {
        this.name = name;
    }

    /**
     * Get the enum video type from video.
     *
     *
     * @param video The video video.
     * @return The enum video type.
     */
    public static EnumVideoSubType fromVideo(Video video) {
        for (EnumVideoSubType type : EnumVideoSubType.values()) {
            if (type == NONE) {
                continue;
            }
            for (String tag : video.getTags()) {
                if (type.name.toLowerCase().equalsIgnoreCase(tag)) {
                    return type;
                }
            }
        }

        return NONE;
    }
}
