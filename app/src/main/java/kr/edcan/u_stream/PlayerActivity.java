package kr.edcan.u_stream;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.prefs.Prefs;
import kr.edcan.u_stream.model.MusicData;
import kr.edcan.u_stream.util.PlayUtil;
import kr.edcan.u_stream.util.YouTubeClient;
import kr.edcan.u_stream.view.SeekArc;

public class PlayerActivity extends AppCompatActivity implements  View.OnTouchListener, SeekArc.OnSeekArcChangeListener{

    @Bind(R.id.toolbar_title)
    TextView toolbarTitle;
    @Bind(R.id.player_tv_played)
    TextView playedTime;
    @Bind(R.id.player_sound_seekbar)
    SeekBar volumeBar;
    @Bind(R.id.player_repeat_type)
    ImageButton rpType;

    public static final Handler handler = new Handler();
    Context mContext;

    public static TextView playingTitle;
    public static TextView playingSubtitle;
    public static ImageButton playBtn;
    public static SeekArc timeProgressBar;

    public static TextView totalTime;
    public static ImageView thumbnail;
    int[] types = new int[]{R.drawable.ic_repeat_on, R.drawable.ic_repeat_one_on, R.drawable.ic_shuffle_on};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);
        Logger.init("asdf");
        mContext = this;
        toolbarTitle.setText("지금 재생중");

        initLayout();
        initProgressBar();
        initVolCtrl();

        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            String url = intent.getDataString();
            String videoId = YouTubeClient.extractYTId(url);
            if(url != null && videoId != null && !videoId.equals("")){
                new MusicDataFromUrl(url, videoId).execute();
            }else{
                Toast.makeText(mContext, "재생 불가능한 url 입니다.",Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void initLayout() {
        playBtn = (ImageButton) findViewById(R.id.player_control_play);
        playingTitle = (TextView) findViewById(R.id.player_tv_title);
        playingSubtitle = (TextView) findViewById(R.id.player_tv_subtitle);
        timeProgressBar = (SeekArc) findViewById(R.id.player_seek);
        thumbnail = (ImageView) findViewById(R.id.player_thumbnail);
        totalTime = (TextView) findViewById(R.id.player_tv_total);
        if(PlayService.mediaPlayer != null && PlayService.nowPlaying != null){
            PlayService.updateState(new Pair<>(PlayService.nowPlaying.getTitle(), PlayService.nowPlaying.getUploader()));
        }
        rpType.setImageResource(types[Prefs.with(this).readInt("repeatType", 0)]);
    }

    // 프로그레스바의 초기화
    private void initProgressBar() {
        timeProgressBar.setMax(100);
        timeProgressBar.setProgress(0);
        timeProgressBar.setOnTouchListener(this);
        timeProgressBar.setOnSeekArcChangeListener(this);
        timeProgressBar.setSecondaryProgress(0);
        PlayService.updateTimePrg();
    }
    // 볼륨 컨트롤러 초기화 & 리스너
    private void initVolCtrl() {
        try{
            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeBar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 1);
            volumeBar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));

            volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                public void onStopTrackingTouch(SeekBar arg0){
                }
                public void onStartTrackingTouch(SeekBar arg0){
                }
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2){
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @OnClick({R.id.player_control_play, R.id.player_control_rewind, R.id.player_control_forward}) void playControl(View v){
        if (PlayService.playable) {
            switch (v.getId()) {
                case R.id.player_control_play:
                    PlayService.doPlay();
                    break;
                case R.id.player_control_rewind:
                    PlayUtil.playOther(this, false);
                    break;
                case R.id.player_control_forward:
                    PlayUtil.playOther(this, true);
                    break;
            }
        } else {
            //준비중
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static void primarySeekBarProgressUpdater() {
        if(timeProgressBar != null) {
            if (PlayService.mediaPlayer != null) {
                if (!timeProgressBar.isTouching()) {
                    int progress = (int) (((float) PlayService.mediaPlayer.getCurrentPosition() / 1000));
                    timeProgressBar.setProgress((progress > 0) ? progress : 0);
                    timeProgressBar.setSecondaryProgress(PlayService.buffer);
                }
                if (!PlayService.playable) {
                    timeProgressBar.setProgress(0);
                    timeProgressBar.setSecondaryProgress(0);
                }
            } else {
                timeProgressBar.setProgress(0);
                timeProgressBar.setSecondaryProgress(0);
            }
        }
        Runnable notification = new Runnable() {
            public void run() {
                primarySeekBarProgressUpdater();
            }
        };
        handler.postDelayed(notification,1000);
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (v.getId() == R.id.player_seek) {
                /** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
                if (PlayService.mediaPlayer != null) {
                    SeekArc sb = (SeekArc) v;
                    int playPositionInMillisecconds = sb.getProgress() * 1000;
                    PlayService.mediaPlayer.seekTo(playPositionInMillisecconds);
                    Logger.e("touched :" + playPositionInMillisecconds);
                }
            }
        }
        return false;
    }
    @Override
    public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
        if(PlayService.mediaPlayer != null)
            playedTime.setText(PlayUtil.parseTime(PlayService.mediaPlayer.getCurrentPosition()));
    }
    @Override
    public void onStartTrackingTouch(SeekArc seekArc) {

    }
    @Override
    public void onStopTrackingTouch(SeekArc seekArc) {

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                volumeBar.setProgress(volumeBar.getProgress()-1);
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                volumeBar.setProgress(volumeBar.getProgress()+1 );
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    @OnClick(R.id.toolbar_back) void back(){
        onBackPressed();
    }
    class MusicDataFromUrl extends AsyncTask<String, String, String> {
        String url, videoId;
        MusicData mData;
        public MusicDataFromUrl(String url, String videoId){
            this.url = url;
            this.videoId = videoId;
        }
        @Override
        protected String doInBackground(String... params) {
            Document doc = null;
            try {
                doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36").get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.init("asdf");
            Logger.i(doc.toString());
            String title = doc.select("span.watch-title").text();
            String uploader = doc.select("div.yt-user-info").text();
            String thumbnail = "https://i.ytimg.com/vi/" + videoId + "/maxresdefault.jpg";
            Logger.d(thumbnail);
            mData = new MusicData(title, videoId, uploader, thumbnail);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            PlayUtil.runService(mContext, mData, true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.player_repeat_type) void changeType(View v){
        Prefs.with(this).writeInt("repeatType", (Prefs.with(this).readInt("repeatType", 0) + 1) % 3);
        int type = Prefs.with(this).readInt("repeatType",0);
        ((ImageButton)v).setImageResource(types[type]);
        if(PlayService.nowPlaying != null){
            PlayUtil.setPlayingList(this, PlayService.nowPlaying);
        }
//        if()
//            PlayUtil.setPlayingList(type, PlayService.nowPlaying);
    }
}
