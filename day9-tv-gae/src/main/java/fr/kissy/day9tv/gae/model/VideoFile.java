package fr.kissy.day9tv.gae.model;

import com.googlecode.objectify.annotation.Unindex;
import fr.kissy.day9tv.gae.enums.EnumVideoMediaType;
import fr.kissy.day9tv.gae.proto.VideoPartsProto;

import java.io.Serializable;

public class VideoFile implements Serializable {
    private static final long serialVersionUID = 1L;

    @Unindex
    private String file;
    @Unindex
    private EnumVideoMediaType type;
    @Unindex
    private Integer width;
    @Unindex
    private Integer height;
    @Unindex
    private Integer duration;
    @Unindex
    private Long size;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public EnumVideoMediaType getType() {
        return type;
    }

    public void setType(EnumVideoMediaType type) {
        this.type = type;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public VideoPartsProto.VideoParts.VideoPart.VideoFile.Builder toBuilder() {
        VideoPartsProto.VideoParts.VideoPart.VideoFile.Builder videoFileProto = VideoPartsProto.VideoParts.VideoPart.VideoFile.newBuilder();
        videoFileProto.setFile(file);
        videoFileProto.setType(VideoPartsProto.VideoParts.VideoPart.VideoFile.VideoMediaType.valueOf(type.name()));
        videoFileProto.setWidth(width);
        videoFileProto.setHeight(height);
        videoFileProto.setDuration(duration);
        videoFileProto.setSize(size);
        return videoFileProto;
    }
}
