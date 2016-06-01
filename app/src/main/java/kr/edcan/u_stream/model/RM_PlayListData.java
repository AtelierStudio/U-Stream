package kr.edcan.u_stream.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by LNTCS on 2016-03-16.
 */
public class RM_PlayListData extends RealmObject {
    @PrimaryKey
    private int id;
    private String title;

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
}
