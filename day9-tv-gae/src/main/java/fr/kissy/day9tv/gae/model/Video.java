package fr.kissy.day9tv.gae.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Unindex;
import fr.kissy.day9tv.gae.dto.VideoEntryDTO;
import fr.kissy.day9tv.gae.enums.EnumVideoSubType;
import fr.kissy.day9tv.gae.enums.EnumVideoType;
import fr.kissy.day9tv.gae.proto.VideoPartsProto;
import fr.kissy.day9tv.gae.proto.VideosProto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Video implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long timestamp;
    @Index
    private EnumVideoType type;
    @Unindex
    private EnumVideoSubType subType;
    private String title;
    private String subTitle;
    @Unindex
    private String description;

    @Unindex
    private List<VideoPart> videoParts;
    @Unindex
    private Set<String> tags;

    /**
     * Default constructor.
     */
    public Video() {
        videoParts = new ArrayList<VideoPart>();
        tags = new HashSet<String>();
    }

    public Video(VideoEntryDTO videoEntryDTO) {
        this();
        timestamp = videoEntryDTO.getTimestamp();
        if (videoEntryDTO.getTitle().contains(" - ") || videoEntryDTO.getTitle().contains(" – ")) {
            String[] splitTitle = videoEntryDTO.getTitle().split(" [-–] ");
            title = splitTitle[0];
            subTitle = splitTitle[1];
        } else {
            title = videoEntryDTO.getTitle();
            subTitle = null;
        }
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public EnumVideoType getType() {
        return type;
    }

    public void setType(EnumVideoType type) {
        this.type = type;
    }

    public EnumVideoSubType getSubType() {
        return subType;
    }

    public void setSubType(EnumVideoSubType subType) {
        this.subType = subType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<VideoPart> getVideoParts() {
        return videoParts;
    }

    public void addVideoPart(VideoPart videoPart) {
        videoParts.add(videoPart);
    }

    public void setVideoParts(List<VideoPart> videoParts) {
        this.videoParts = videoParts;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * Check if the given object is a part of the current object.
     *
     * @param o The object to test.
     * @return True or false.
     */
    public boolean partEquals(Object o) {
        if (this == o) return false;
        if (!(o instanceof Video)) return false;

        Video video = (Video) o;

        return !((type == null && video.getType() == null) || (video.getType() != null && !video.getType().equals(type)))
                && !((title == null && video.getTitle() == null) || (video.getTitle() != null && !video.getTitle().equals(title)))
                && !((subTitle == null && video.getSubTitle() == null) || (video.getSubTitle() != null && !video.getSubTitle().equals(subTitle)));

    }

    /**
     * Merge the data to the videoProto.
     *
     * @return The generated builder.
     */
    public VideosProto.Videos.Video.Builder toBuilder() {
        VideosProto.Videos.Video.Builder videoProto = VideosProto.Videos.Video.newBuilder();

        videoProto.setTimestamp(timestamp);
        videoProto.setType(type.name());
        videoProto.setSubType(subType.name());
        videoProto.setTitle(title);
        videoProto.setSubtitle(subTitle);
        videoProto.setDescription(description);

        VideoPartsProto.VideoParts.Builder videopartsProto = VideoPartsProto.VideoParts.newBuilder();
        for (VideoPart videoPart : videoParts) {
            videopartsProto.addVideoParts(videoPart.toBuilder());
        }

        videoProto.setVideoParts(videopartsProto.build().toByteString());

        return videoProto;
    }
}
