package kr.edcan.u_stream.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;

import io.realm.Realm;
import io.realm.RealmResults;
import kr.edcan.u_stream.R;
import kr.edcan.u_stream.model.RM_MusicData;
import kr.edcan.u_stream.model.RM_PlayListData;

/**
 * Created by LNTCS on 2016-03-11.
 */
public class PlayListGridAdapter  extends BaseAdapter {
    LayoutInflater mInflater;
    Context mContext;
    RealmResults<RM_PlayListData> pList;
    Realm realm;
    public PlayListGridAdapter(Context context){
        Logger.init("ASDF");
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        realm = Realm.getInstance(mContext);
        pList = realm.where(RM_PlayListData.class).findAll();
    }
    @Override
    public int getCount() {
        return pList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.content_playlist_grid, parent, false);
        }

        RM_PlayListData pData = pList.get(position);
        RealmResults<RM_MusicData> musicList = realm.where(RM_MusicData.class).equalTo("playListId",pData.getId()).findAll();

        ImageView thumb = (ImageView) convertView.findViewById(R.id.playlist_grid_thumb);
        TextView title = (TextView) convertView.findViewById(R.id.playlist_grid_title);
        TextView count = (TextView) convertView.findViewById(R.id.playlist_grid_count);

        title.setText(pData.getTitle());
        count.setText(musicList.size() + "곡이 플레이 리스트에 있음");

        if(musicList.size() != 0){
            String thumbUrl = musicList.get((int) (Math.random()*musicList.size())).getThumbnail();

            Glide.with(mContext)
                    .load(thumbUrl)
                    .crossFade()
                    .into(thumb);
        }

        return convertView;
    }
}