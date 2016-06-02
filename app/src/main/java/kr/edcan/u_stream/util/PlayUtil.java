package kr.edcan.u_stream.util;

import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;

import kr.edcan.u_stream.PlayService;
import kr.edcan.u_stream.model.MusicData;

/**
 * Created by LNTCS on 2016-05-27.
 */
public class PlayUtil {
    public static final String STARTFOREGROUND_ACTION = "kr.edcan.u_stream.action.startforeground";
    public static final String RESUMEFOREGROUND_ACTION = "kr.edcan.u_stream.action.resume";
    public static final String STOPFOREGROUND_ACTION = "kr.edcan.u_stream.action.stopforeground";
    public static void runService(Context context, MusicData musicData, boolean isStart){
        PlayService.setNowPlaying(musicData);
        if(PlayService.mediaPlayer == null){
            Intent service = new Intent(context, PlayService.class);
            service.putExtra("isStart", isStart);
            service.setAction(STARTFOREGROUND_ACTION);
            context.startService(service);
        }else{
            PlayService.setNowPlaying(musicData);
            new PlayService.getPlayUrlSync(isStart).execute();
        }
    }
    public static void stopForeground(Context context){
        Intent service = new Intent(context, PlayService.class);
        service.setAction(STOPFOREGROUND_ACTION);
        context.startService(service);
        Logger.d("STOP FOREGROUND SERVICE");
    }
    public static void resumeForeground(Context context){
        Intent service = new Intent(context, PlayService.class);
        service.setAction(RESUMEFOREGROUND_ACTION);
        context.startService(service);
        Logger.d("RESUME FOREGROUND SERVICE");
    }
}