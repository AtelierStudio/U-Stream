package kr.edcan.u_stream;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.edcan.u_stream.view.SeekArc;

public class PlayerActivity extends AppCompatActivity implements  View.OnTouchListener, SeekArc.OnSeekArcChangeListener{

    @Bind(R.id.toolbar_title)
    TextView toolbarTitle;
    @Bind(R.id.player_seek)
    SeekArc timeProgressBar;
    @Bind(R.id.player_thumbnail)
    ImageView thumbnail;
    @Bind(R.id.player_tv_total)
    TextView totalTime;
    @Bind(R.id.player_tv_played)
    TextView playedTime;
    @Bind(R.id.player_sound_seekbar)
    SeekBar volumeBar;

    private int mediaFileLengthInMilliseconds;
    private final Handler handler = new Handler();
    Context mContext;

    public static TextView playingTitle;
    public static TextView playingSubtitle;
    public static ImageButton playBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);
        mContext = this;
        toolbarTitle.setText("지금 재생중");
        if(PlayService.mediaPlayer == null)
            return;
        initProgressBar();
        initVolCtrl();
        playerSet();
    }

    private void initProgressBar() {
        timeProgressBar.setMax(100);
        timeProgressBar.setProgress(0);
        timeProgressBar.setOnTouchListener(this);
        timeProgressBar.setOnSeekArcChangeListener(this);
        timeProgressBar.setSecondaryProgress(0);
    }

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

    @OnClick(R.id.player_control_play) void playControl(View v){
        if(PlayService.playable) {
            PlayService.doPlay();
        }else{
            //준비중
        }
    }

    private void playerSet(){
        playBtn = (ImageButton) findViewById(R.id.player_control_play);
        playingTitle = (TextView) findViewById(R.id.player_tv_title);
        playingSubtitle = (TextView) findViewById(R.id.player_tv_subtitle);
        if(PlayService.mediaPlayer != null)
            playBtn.setImageResource((PlayService.mediaPlayer.isPlaying())?R.drawable.ic_pause: R.drawable.ic_play);
        Glide.with(mContext).load(PlayService.nowPlaying.getThumbnail()).asBitmap().placeholder(R.drawable.bg_default_album).into(thumbnail);
        mediaFileLengthInMilliseconds = PlayService.mediaPlayer.getDuration(); // gets the song length in milliseconds from URL
        timeProgressBar.setMax(mediaFileLengthInMilliseconds/1000);
        timeProgressBar.setOnTouchListener(this);
        timeProgressBar.setOnSeekArcChangeListener(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                totalTime.setText(parseTime(mediaFileLengthInMilliseconds));
                playingTitle.setText(PlayService.nowPlaying.getTitle());
                playingTitle.setSelected(true);
                playingSubtitle.setText(PlayService.nowPlaying.getUploader());
            }
        });
        primarySeekBarProgressUpdater();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void primarySeekBarProgressUpdater() {
        if(!timeProgressBar.isTouching()) {
            timeProgressBar.setProgress((int) (((float) PlayService.mediaPlayer.getCurrentPosition() / 1000))); // This math construction give a percentage of "was playing"/"song length"
            timeProgressBar.setSecondaryProgress(PlayService.buffer);
        }
        if (PlayService.mediaPlayer != null) {
            Runnable notification = new Runnable() {
                public void run() {
                    primarySeekBarProgressUpdater();
                }
            };
            handler.postDelayed(notification,1000);
        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.player_seek){
            /** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
            if(PlayService.mediaPlayer != null){
                SeekArc sb = (SeekArc) v;
                int playPositionInMillisecconds = sb.getProgress() * 1000;
                PlayService.mediaPlayer.seekTo(playPositionInMillisecconds);
            }
        }
        return false;
    }
    public String parseTime(long ms){
        long millis = ms;
        if(millis >= 3600000){
            String time = String.format("%d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.HOURS.toHours(TimeUnit.MILLISECONDS.toDays(millis)),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            return time;
        }else{
            String time = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            return time;
        }
    }
    @Override
    public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
        playedTime.setText(parseTime(PlayService.mediaPlayer.getCurrentPosition()));
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
}
