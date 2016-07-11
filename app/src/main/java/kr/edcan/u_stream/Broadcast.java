package kr.edcan.u_stream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Pair;

/**
 * Created by LNTCS on 2016-07-11.
 */
public class Broadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())){
            if(PlayService.mediaPlayer != null) {
                PlayService.mediaPlayer.pause();
                PlayService.updateState(
                        new Pair<>(
                                PlayService.nowPlaying.getTitle(),
                                PlayService.nowPlaying.getUploader()
                        )
                );
            }
        }
    }
}
