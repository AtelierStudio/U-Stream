package kr.edcan.u_stream.util;

import android.content.Context;
import android.content.Intent;

import java.util.concurrent.TimeUnit;

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
            PlayService.getPlayUrlSync(isStart);
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