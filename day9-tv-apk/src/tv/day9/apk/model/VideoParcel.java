package tv.day9.apk.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import tv.day9.apk.provider.impl.VideoDAO;

public class VideoParcel implements Parcelable {

    public static final Parcelable.Creator<VideoParcel> CREATOR = new Parcelable.Creator<VideoParcel>() {
        public VideoParcel createFromParcel(Parcel in) {
            return new VideoParcel(in);
        }

        public VideoParcel[] newArray(int size) {
            return new VideoParcel[size];
        }
    };

    private Long timestamp;
    private String type;
    private String title;
    private String subtitle;
    private String description;
    private String videoParts;
    private int favorite;

    /**
     * Default constructor.
     */
    public VideoParcel() {
    }

    public VideoParcel(Parcel parcel) {
        readFromParcel(parcel);
    }

    /**
     * Constructor.
     *
     * @param cursor The cursor.
     */
    public VideoParcel(Cursor cursor) {
        this.timestamp = cursor.getLong(VideoDAO.CONTENT_ID_COLUMN);
        this.type = cursor.getString(VideoDAO.CONTENT_TYPE_COLUMN);
        this.title = cursor.getString(VideoDAO.CONTENT_TITLE_COLUMN);
        this.subtitle = cursor.getString(VideoDAO.CONTENT_SUBTITLE_COLUMN);
        this.description = cursor.getString(VideoDAO.CONTENT_DESCRIPTION_COLUMN);
        this.videoParts = cursor.getString(VideoDAO.CONTENT_VIDEO_PARTS_COLUMN);
        this.favorite = cursor.getInt(VideoDAO.CONTENT_FAVORITE_COLUMN);
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoParts() {
        return videoParts;
    }

    public void setVideoParts(String videoParts) {
        this.videoParts = videoParts;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
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
        parcel.writeLong(timestamp);
        parcel.writeString(type);
        parcel.writeString(title);
        parcel.writeString(subtitle);
        parcel.writeString(description);
        parcel.writeInt(favorite);
        parcel.writeString(videoParts);
    }

    private void readFromParcel(Parcel parcel) {
        timestamp = parcel.readLong();
        type = parcel.readString();
        title = parcel.readString();
        subtitle = parcel.readString();
        description = parcel.readString();
        favorite = parcel.readInt();
        videoParts = parcel.readString();
    }
}
