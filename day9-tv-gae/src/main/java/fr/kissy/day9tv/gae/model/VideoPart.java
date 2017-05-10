package fr.kissy.day9tv.gae.model;

import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Unindex;
import fr.kissy.day9tv.gae.proto.VideoPartsProto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Embed
public class VideoPart implements Serializable {
    private static final long serialVersionUID = 1L;

    @Unindex
    private Long id;
    @Unindex
    private Integer part;
    @Unindex
    @Serialize
    private List<VideoFile> files;

    public VideoPart() {
        files = new ArrayList<VideoFile>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPart() {
        return part;
    }

    public void setPart(Integer part) {
        this.part = part;
    }

    public List<VideoFile> getFiles() {
        return files;
    }

    public void setFiles(List<VideoFile> files) {
        this.files = files;
    }

    public VideoPartsProto.VideoParts.VideoPart.Builder toBuilder() {
        VideoPartsProto.VideoParts.VideoPart.Builder videoPartProto = VideoPartsProto.VideoParts.VideoPart.newBuilder();
        videoPartProto.setId(id);
        videoPartProto.setPart(part);

        for (VideoFile videoFile : files) {
            // Prevent displaying weird videos.
            if (videoFile.getType() != null) {
                videoPartProto.addFiles(videoFile.toBuilder());
            }
        }

        return videoPartProto;
    }
}
