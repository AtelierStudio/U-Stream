package kr.edcan.u_stream;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Pair;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.orhanobut.logger.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import kr.edcan.u_stream.model.MusicData;
import kr.edcan.u_stream.util.PlayUtil;
import kr.edcan.u_stream.util.YouTubeClient;

/**
 * Created by LNTCS on 2016-03-22.
 */
public class PlayService extends Service {

    public static final int NOTIFICATION_NUM = 3939;
    public static MediaPlayer mediaPlayer;
    public static MusicData nowPlaying;
    public static boolean playable = false;
    public static Notification notification;
    public static long beforeEvent;
    public static  Handler handler;
    public static String rawUrl = "https://www.youtube.com/watch?v=";
    public static Context mContext;
    public static int buffer = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mContext = this;
        handler = new Handler();
        notification = new Notification(R.drawable.ic_noti, "μ'Stream", System.currentTimeMillis());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        if(intent==null) return 0;
        if (intent.getAction().equals(PlayUtil.STARTFOREGROUND_ACTION)) {
            notification = new Notification(R.drawable.ic_noti, "μ'Stream", System.currentTimeMillis());
            Intent i = new Intent(this, PlayerActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notification.contentIntent = pi;
            final RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.content_notification);
            notification.contentView = views;
            new getPlayUrlSync((intent == null) ? false : intent.getBooleanExtra("isStart", false)).execute();
            startForeground(NOTIFICATION_NUM,notification);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class getPlayUrlSync extends AsyncTask<String,String,String> {
        boolean isStart;
        public getPlayUrlSync(boolean isStart){
            this.isStart = isStart;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            updateLoading();
        }

        private void updateLoading() {
            playable = false;
            setStatus(new Pair<>("음원 불러오는 중...",""));
            notification.flags = Notification.FLAG_AUTO_CANCEL;
        }

        @Override
        protected String doInBackground(String... params) {
            Logger.e(YouTubeClient.API_URL + rawUrl + nowPlaying.getVideoId());
            String result="";
            Document doc = null;
            try {
                doc = Jsoup.connect(YouTubeClient.API_URL + rawUrl + nowPlaying.getVideoId()).get();
                return YouTubeClient.REDIRECT_URL + doc.select("a#downloadMP4").get(0).attr("href");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("")){
                Toast.makeText(mContext, "죄송합니다.\n재생할 수 없는 영상입니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            Logger.e(s);
            playSet(s, isStart);
            super.onPostExecute(s);
        }
    }
    static void playSet(String url, final boolean isStart){
        Logger.e(url);
        new setSourceTask(url, isStart).execute();
    }

    static class setSourceTask extends AsyncTask<String, String, String>{
        String url;
        boolean isStart;
        public setSourceTask(String url, boolean isStart){
            this.url = url;
            this.isStart = isStart;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    if(percent == 0) percent = 1;
                    buffer = percent;
                }
            });
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    playable = true;
                    if(isStart) {
                        mediaPlayer.start();
                    }
                    setInfo();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    setInfo();
                }
            });
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
    static void setInfo(){
        setStatus(new Pair<>(nowPlaying.getTitle(),nowPlaying.getUploader()));
        setNotify();
        setMainBar();
        setPlayerStatus();
    }

    private static void setPlayerStatus() {
        if (PlayerActivity.playBtn != null){
            PlayerActivity.playBtn.setImageResource((mediaPlayer.isPlaying())?R.drawable.ic_pause: R.drawable.ic_play);
        }
    }

    private static void setMainBar() {
        if (MainActivity.playBtn != null){
            MainActivity.playBtn.setImageResource((mediaPlayer.isPlaying())?R.drawable.ic_btm_pause: R.drawable.ic_btm_play);
        }
    }

    private static void setStatus(Pair<String, String> info){
        RemoteViews rv = notification.contentView;
        rv.setTextViewText(R.id.notify_title, info.first);
        rv.setTextViewText(R.id.notify_subtitle, info.second);
        if(MainActivity.playingTitle != null && MainActivity.playingSubtitle != null){
            MainActivity.playingTitle.setText(info.first);
            MainActivity.playingSubtitle.setText(info.second);
        }
        if(PlayerActivity.playingTitle != null && PlayerActivity.playingSubtitle != null){
            if(!PlayerActivity.playingTitle.getText().equals(info.first)) {
                PlayerActivity.playingTitle.setText(info.first);
                PlayerActivity.playingTitle.setSelected(true);
            }
            PlayerActivity.playingSubtitle.setText(info.second);
        }
    }

    private static void setNotify() {
        RemoteViews rv = notification.contentView;
        rv.setImageViewResource(R.id.notify_play, (mediaPlayer.isPlaying())?R.drawable.selector_notify_pause: R.drawable.selector_notify_play);
        notification.flags = (mediaPlayer.isPlaying())?Notification.FLAG_ONGOING_EVENT: Notification.FLAG_AUTO_CANCEL;
        NotificationTarget notificationTarget = new NotificationTarget(mContext,rv,R.id.notify_thumb,notification,NOTIFICATION_NUM);
        Glide.with(mContext).load(nowPlaying.getThumbnail()).asBitmap().into(notificationTarget);

        Intent intent_ = new Intent("kr.edcan.ustream.control");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent_,
                PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.notify_play, pendingIntent);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("kr.edcan.ustream.control");
        mContext.registerReceiver( new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long currentEvent = System.currentTimeMillis();
                if(beforeEvent < currentEvent - 100) {
                    doPlay();
                }
                beforeEvent = currentEvent;
            }
        }, intentFilter);
    }

    public static void doPlay(){
        RemoteViews rv = notification.contentView;
        if (PlayService.mediaPlayer.isPlaying()) {
            rv.setImageViewResource(R.id.notify_play, R.drawable.selector_notify_play);
            PlayService.mediaPlayer.pause();
        } else {
            rv.setImageViewResource(R.id.notify_play, R.drawable.selector_notify_pause);
            PlayService.mediaPlayer.start();
        }
        setInfo();
    }

    public static void setNowPlaying(MusicData nowPlaying) {
        PlayService.nowPlaying = nowPlaying;
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }
}