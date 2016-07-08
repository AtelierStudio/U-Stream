package kr.edcan.u_stream.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import kr.edcan.u_stream.R;
import kr.edcan.u_stream.model.MusicData;
import kr.edcan.u_stream.util.DialogUtil;
import kr.edcan.u_stream.util.PlayUtil;

/**
 * Created by LNTCS on 2016-03-16.
 */
public class PlaylistListAdapter extends ArrayAdapter<MusicData> {
    // 레이아웃 XML을 읽어들이기 위한 객체
    private LayoutInflater mInflater;
    Context mContext;
    String playlistTitle;

    public PlaylistListAdapter(Context context, ArrayList<MusicData> object, CharSequence title){
        // 상위 클래스의 초기화 과정
        // context, 0, 자료구조
        super(context,0,object);
        mContext = context;
        mInflater=(LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.playlistTitle = title.toString();
    }

    private PlaylistListAdapter getAdapter(){
        return this;
    }
    @Override
    public View getView(final int position, View v, ViewGroup parent){
        View view = null;
        if (v == null) {
            view = mInflater.inflate(R.layout.content_playlist_list, null);
            view.setTag(position);
        } else {
            view = v;
        }

        final MusicData data=this.getItem(position);

        if(data!=null){
            final TextView title = (TextView)view.findViewById(R.id.playlist_list_title);
            TextView uploader = (TextView)view.findViewById(R.id.playlist_list_uploader);
            ImageView thumb = (ImageView) view.findViewById(R.id.playlist_list_img);
            ImageButton delete = (ImageButton)view.findViewById(R.id.playlist_list_delete);
            ImageButton play = (ImageButton)view.findViewById(R.id.playlist_list_play);

            title.setText(data.getTitle());
            uploader.setText(data.getUploader());
            Glide.with(mContext)
                    .load(data.getThumbnail())
                    .crossFade()
                    .into(thumb);

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.json(new Gson().toJson(data));
                    PlayUtil.runService(mContext, data, true);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogUtil.deletePlayListDialog(mContext, data, playlistTitle, getAdapter());
                }
            });
        }
        return view;
    }
}