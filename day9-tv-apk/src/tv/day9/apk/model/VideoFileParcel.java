package tv.day9.apk.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * - .
 *
 * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
 * @version $Id: VideoFileParcel.java 204 2012-01-27 17:03:20Z kissy $
 */
public class VideoFileParcel implements Parcelable {

    public static final Parcelable.Creator<VideoFileParcel> CREATOR = new Parcelable.Creator<VideoFileParcel>() {
        public VideoFileParcel createFromParcel(Parcel in) {
            return new VideoFileParcel(in);
        }

        public VideoFileParcel[] newArray(int size) {
            return new VideoFileParcel[size];
        }
    };

    private String title;
    private String description;
    private String file;
    private String type;
    private long size;
    private int height;
    private int width;
    private int duration;
    private int videoPart;

    /**
     * Default constructor.
     */
    public VideoFileParcel() {
    }

    /**
     * Default constructor.
     *
     * @param title The title.
     * @param description The description.
     * @param file The file.
     * @param type The type.
     * @param size The size.
     * @param height The height.
     * @param width The width.
     * @param duration The duration.
     * @param videoPart The videoPart.
     */
    public VideoFileParcel(String title, String description, String file, String type, long size, int height, int width, int duration, int videoPart) {
        this.title = title;
        this.description = description;
        this.file = file;
        this.type = type;
        this.size = size;
        this.height = height;
        this.width = width;
        this.duration = duration;
        this.videoPart = videoPart;
    }

    public VideoFileParcel(Parcel parcel) {
        readFromParcel(parcel);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getVideoPart() {
        return videoPart;
    }

    public void setVideoPart(int videoPart) {
        this.videoPart = videoPart;
    }

    /**
     * @inheritDoc
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(file);
        parcel.writeString(type);
        parcel.writeLong(size);
        parcel.writeInt(height);
        parcel.writeInt(width);
        parcel.writeInt(duration);
        parcel.writeInt(videoPart);
    }

    private void readFromParcel(Parcel parcel) {
        title = parcel.readString();
        description = parcel.readString();
        file = parcel.readString();
        type = parcel.readString();
        size = parcel.readLong();
        height = parcel.readInt();
        width = parcel.readInt();
        duration = parcel.readInt();
        videoPart = parcel.readInt();
    }
}
