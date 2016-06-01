package kr.edcan.u_stream.model;

/**
 * Created by LNTCS on 2016-03-15.
 */
public class SearchData {
    private String id;
    private String title;
    private String uploader = "";
    private String description;
    private String thumbnail;

    public SearchData(String id, String title, String description, String thumbnail, String uploader) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;
        this.uploader = uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUploader() {
        return uploader;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}