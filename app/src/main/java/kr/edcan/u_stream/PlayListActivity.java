package kr.edcan.u_stream;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import kr.edcan.u_stream.adapter.PlaylistListAdapter;
import kr.edcan.u_stream.model.MusicData;
import kr.edcan.u_stream.model.RM_MusicData;

/**
 * Created by LNTCS on 2016-07-08.
 */
public class PlayListActivity extends AppCompatActivity {

    @Bind(R.id.toolbar_title)
    TextView toolbarTitle;
    @Bind(R.id.playlist_list)
    ListView listView;

    ArrayList<MusicData> mList = new ArrayList<>();

    private Realm realm;
    private RealmConfiguration realmConfig;
    PlaylistListAdapter playlistListAdapter;

    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        ButterKnife.bind(this);
        Logger.init("asdf");
        mContext = this;
        toolbarTitle.setText(getIntent().getStringExtra("title"));
        realmConfig = new RealmConfiguration.Builder(mContext).build();
        realm = Realm.getInstance(realmConfig);
        for(RM_MusicData data : realm.where(RM_MusicData.class).equalTo("playListId", getIntent().getIntExtra("id",0)).findAll()){
            mList.add(new MusicData(data));
        }
        playlistListAdapter = new PlaylistListAdapter(mContext, mList, toolbarTitle.getText());
        listView.setAdapter(playlistListAdapter);
    }

    @OnClick(R.id.toolbar_back) void back(){
        onBackPressed();
    }
}
