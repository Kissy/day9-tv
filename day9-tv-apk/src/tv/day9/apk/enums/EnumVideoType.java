package tv.day9.apk.enums;

/**
 * Enum video type.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: EnumVideoType.java 221 2012-03-21 08:06:44Z kissy $
 */
public enum EnumVideoType {

    // Day9 Daily
    DAY9_DAILY,
    // GSPA
    GSPA,
    // AHGL
    AHGL,
    // Dreamhack
    DREAMHACK,
    // CSL
    CSL,
    // Amazon
    AMAZON,
    // Root Gamin's WARZONE
    ROOT_GAMINGS_WARZONE,
    // Blizzard In-House
    BLIZZARD_IN_HOUSE_TOURNAMENT,
    // King of the beta
    KING_OF_THE_BETA,
    // ZOTAC
    ZOTAC13,
    // Red Bull Lan
    RED_BULL_LAN,
    // Monobattle
    MONOBATTLE,

    // Other games
    DIABLO_III_BETA,
    ELDER_SCROLLS_V,
    AMNESIA,

    OTHER;

    /**
     * Get the enum video type from title.
     *
     * @param title The video title.
     * @return The enum video type.
     */
    public static EnumVideoType fromString(String title) {
        if (title == null || title.length() == 0) {
            return OTHER;
        }

        String titleUpper = title.toUpperCase();
        for (EnumVideoType type : EnumVideoType.values()) {
            if (titleUpper.startsWith(type.name())) {
                return type;
            }
        }

        return OTHER;
    }
}
