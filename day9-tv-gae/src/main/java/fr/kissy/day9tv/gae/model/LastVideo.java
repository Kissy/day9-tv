package fr.kissy.day9tv.gae.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

import java.io.Serializable;

/**
 * Last video DTO to save in Cache.
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: LastVideo.java 223 2013-04-22 23:15:38Z Kissy $
 */
@Cache
@Entity
public class LastVideo implements Serializable {
    public static final String ID = "lastVideo";

    @Id
    private String id;
    @Unindex
    private Long timestamp;

    public LastVideo() {
        this.id = ID;
        this.timestamp = 0L;
    }

    public LastVideo(Long timestamp) {
        this();
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
