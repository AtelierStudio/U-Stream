package kr.edcan.u_stream.util;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import es.dmoral.prefs.Prefs;
import io.realm.Realm;
import io.realm.RealmResults;
import kr.edcan.u_stream.PlayService;
import kr.edcan.u_stream.model.MusicData;
import kr.edcan.u_stream.model.RM_MusicData;

/**
 * Created by LNTCS on 2016-05-27.
 */
public class PlayUtil {
    public static final String STARTFOREGROUND_ACTION = "kr.edcan.u_stream.action.startforeground";
    public static final String RESUMEFOREGROUND_ACTION = "kr.edcan.u_stream.action.resume";
    public static final String STOPFOREGROUND_ACTION = "kr.edcan.u_stream.action.stopforeground";
    public static Realm realm;
    public static void runService(Context context, MusicData musicData, boolean isStart){
        realm = Realm.getInstance(context);
        Prefs.with(context).write("latestPlay", new Gson().toJson(musicData));
        if(PlayService.mediaPlayer == null){
            // 플레이리스트 세팅, 인덱스 세팅
            setPlayingList(musicData);
            PlayService.setNowPlaying(musicData);
            Intent service = new Intent(context, PlayService.class);
            service.putExtra("isStart", isStart);
            service.setAction(STARTFOREGROUND_ACTION);
            context.startService(service);
        }else{
            if(PlayService.nowPlaying != null && PlayService.nowPlaying.getPlayListId() == musicData.getPlayListId()){
                // 플레이 리스트 아이디가 같을 경우 인덱스의 값만 변경
                setPlayingIndex(musicData);
            }else{
                // 다를경우 리스트 세팅
                setPlayingList(musicData);
            }
            PlayService.setNowPlaying(musicData);
            PlayService.getPlayUrlSync(isStart);
        }
    }
    public static void playOther(Context context, boolean isNext){
        realm = Realm.getInstance(context);
        if(PlayService.mediaPlayer == null)return;
        if(isNext){//다음곡
            PlayService.INDEX++;
            if(PlayService.INDEX > PlayService.playingList.size()-1){
                PlayService.INDEX = 0;
            }
        }else{//이전곡
            PlayService.INDEX--;
            if(PlayService.INDEX < 0){
                PlayService.INDEX = PlayService.playingList.size()-1;
            }
        }
        runService(context, getMusicByIndex(), true);
    }

    private static void setPlayingIndex(MusicData musicData) {
        for(int i = 0 ; i < PlayService.playingList.size() ; ++i){
            int data = PlayService.playingList.get(i);
            if(data == musicData.getId()) {
                PlayService.INDEX = i;
            }
        }
    }
    private static void setPlayingList(MusicData musicData) {
        PlayService.playingList.clear();
        RealmResults<RM_MusicData> result = realm.where(RM_MusicData.class).equalTo("playListId", musicData.getPlayListId()).findAll();
        for(int i = 0 ; i < result.size() ; ++i){
            RM_MusicData data = result.get(i);
            PlayService.playingList.add(data.getId());
        }
//        if() //셔플상태의 경우 뒤섞기
        setPlayingIndex(musicData);
    }
    private static MusicData getMusicByIndex(){
        if(PlayService.playingList.size() <= 1){
            return PlayService.nowPlaying;
        }else{
            return new MusicData(realm.where(RM_MusicData.class).equalTo("id", PlayService.playingList.get(PlayService.INDEX)).findFirst());
        }
    }
    public static void stopForeground(Context context){
        Intent service = new Intent(context, PlayService.class);
        service.setAction(STOPFOREGROUND_ACTION);
        context.startService(service);
    }
    public static void resumeForeground(Context context){
        Intent service = new Intent(context, PlayService.class);
        service.setAction(RESUMEFOREGROUND_ACTION);
        context.startService(service);
    }
    public static String parseTime(long ms){
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
}