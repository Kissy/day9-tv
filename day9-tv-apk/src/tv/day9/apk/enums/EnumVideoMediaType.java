package tv.day9.apk.enums;

/**
 * Video media type.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: EnumVideoMediaType.java 51 2011-09-24 07:08:53Z Kissy $
 */
public enum EnumVideoMediaType {
    SOURCE("Source"),
    BLIP_LD("Blip LD"),
    BLIP_SD("Blip SD"),
    BLIP_HD("Blip HD 720"),
    BLIP_HLS("Blip HLS");

    private String name;

    /**
     * Dedfault constructor.
     *
     * @param name The xml name.
     */
    EnumVideoMediaType(String name) {
        this.name = name;
    }

    /**
     * Get the xml name.
     *
     * @return The xml name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the enum video media type from name.
     *
     * @param name The xml name.
     * @return The enum video media type.
     */
    public static EnumVideoMediaType fromName(String name) {
        for (EnumVideoMediaType type : EnumVideoMediaType.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }

        return null;
    }
}
