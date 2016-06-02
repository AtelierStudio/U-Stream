package kr.edcan.u_stream.model;

/**
 * Created by LNTCS on 2016-03-16.
 */
public class MusicData{
    private int id;
    private String title;
    private String videoId;
    private int playListId;
    private String uploader = "";
    private String description;
    private String thumbnail;

    public MusicData(RM_MusicData mData) {
        this.id = mData.getId();
        this.title = mData.getTitle();
        this.videoId = mData.getVideoId();
        this.playListId = mData.getPlayListId();
        this.uploader = mData.getUploader();
        this.description = mData.getDescription();
        this.thumbnail = mData.getThumbnail();
    }

    public MusicData(SearchData sData) {
        this.title = sData.getTitle();
        this.videoId = sData.getId();
        this.uploader = sData.getUploader();
        this.description = sData.getDescription();
        this.thumbnail = sData.getThumbnail();
    }

    public MusicData(String title, String videoId, String uploader, String thumbnail) {
        this.title = title;
        this.videoId = videoId;
        this.uploader = uploader;
        this.thumbnail = thumbnail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public int getPlayListId() {
        return playListId;
    }

    public void setPlayListId(int playListId) {
        this.playListId = playListId;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
