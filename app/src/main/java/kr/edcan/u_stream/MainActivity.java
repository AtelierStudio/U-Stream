package kr.edcan.u_stream;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.Space;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.prefs.Prefs;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import kr.edcan.u_stream.adapter.MainAdapter;
import kr.edcan.u_stream.model.MusicData;
import kr.edcan.u_stream.util.PlayUtil;

/**
 * Created by LNTCS on 2015-12-29.
 */
public class MainActivity extends AppCompatActivity{

    @Bind(R.id.toolbar_title)
    TextView toolbarTitle;
    @Bind({R.id.main_tab_space, R.id.main_tab_playlist, R.id.main_tab_analog})
    List<TextView> mainTabs;
    @Bind(R.id.main_tab_margin)
    Space tabMargin;
    @Bind(R.id.pager)
    ViewPager pager;

    public static TextView playingTitle;
    public static TextView playingSubtitle;
    public static ImageButton playBtn;

    private Realm realm;
    private RealmConfiguration realmConfig;
    MainAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Logger.init("Logger");
        initBtmBar();
        toolbarTitle.setText("플레이보드");

        adapter = new MainAdapter(this);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(5);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, (position + positionOffset));
                tabMargin.setLayoutParams(param);
                for (TextView tv : mainTabs) {
                    tv.setTextColor(getResources().getColorStateList(R.color.selector_primary_color));
                }
                mainTabs.get(Math.round(position + positionOffset)).setTextColor(getResources().getColor(R.color.colorPrimary));
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);
        if(PlayService.mediaPlayer == null) {
            MusicData latest = new Gson().fromJson(Prefs.with(this).read("latestPlay"), MusicData.class);
            if (latest != null) {
                PlayUtil.runService(this, latest, false);
//                PlayUtil.runService(this, new MusicData(realm.where(RM_MusicData.class).findFirst()), false);
            }
        }else{
            if(PlayService.nowPlaying != null) {
                PlayService.updateState(new Pair<>(PlayService.nowPlaying.getTitle(), PlayService.nowPlaying.getUploader()));
            }
        }
    }
/*
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comming);
        findViewById(R.id.goBeta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/apps/testing/kr.edcan.u_stream")));
            }
        });
    }*/
    private void initBtmBar() {
        playBtn = (ImageButton) findViewById(R.id.main_playing_btn);

        playingTitle = (TextView) findViewById(R.id.main_playing_title);
        playingSubtitle = (TextView) findViewById(R.id.main_playing_subtitle);
    }

    @OnClick({R.id.main_tab_space, R.id.main_tab_playlist, R.id.main_tab_analog})
    public void tabClick(View view) {
        pager.setCurrentItem(mainTabs.indexOf(view));
    }

    @OnClick(R.id.main_tab)
    void nowPlaying(View v) {
        startActivity(new Intent(this, PlayerActivity.class));
    }

    @OnClick(R.id.toolbar_search)
    void search(View v) {
        startActivity(new Intent(this, SearchActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter != null)
            adapter.notifyDataSetChanged();
        if(PlayService.mediaPlayer != null && PlayService.nowPlaying != null){
            PlayService.updateState(new Pair<>(PlayService.nowPlaying.getTitle(), PlayService.nowPlaying.getUploader()));
        }
    }

    @OnClick(R.id.main_playing_btn)
    public void playMusic() {
        if(PlayService.playable) {
            PlayService.doPlay();
        }else{
            //준비중
        }
    }
}
