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

import java.util.ArrayList;

import io.realm.Realm;
import kr.edcan.u_stream.R;
import kr.edcan.u_stream.model.MusicData;
import kr.edcan.u_stream.model.RM_PlayListData;
import kr.edcan.u_stream.model.SearchData;
import kr.edcan.u_stream.util.DialogUtil;
import kr.edcan.u_stream.util.PlayUtil;

/**
 * Created by LNTCS on 2016-03-16.
 */
public class SearchResultListAdapter extends ArrayAdapter<SearchData> {
    // 레이아웃 XML을 읽어들이기 위한 객체
    private LayoutInflater mInflater;
    Context mContext;
    int type;
    public SearchResultListAdapter(Context context, ArrayList<SearchData> object, int type){
        // 상위 클래스의 초기화 과정
        // context, 0, 자료구조
        super(context,0,object);
        mContext = context;
        mInflater=(LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.type = type;
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent){
        View view = null;
        if (v == null) {
            view = mInflater.inflate(R.layout.content_search_result, null);
            view.setTag(position);
        } else {
            view = v;
        }

        final SearchData data=this.getItem(position);

        if(data!=null){
            TextView title = (TextView)view.findViewById(R.id.search_result_title);
            TextView uploader = (TextView)view.findViewById(R.id.search_result_uploader);
            ImageView thumb = (ImageView) view.findViewById(R.id.search_result_img);
            ImageButton add = (ImageButton)view.findViewById(R.id.search_result_add);
            ImageButton play = (ImageButton)view.findViewById(R.id.search_result_play);

            title.setText(data.getTitle());
            uploader.setText(data.getUploader());
            Glide.with(mContext)
                    .load(data.getThumbnail())
                    .crossFade()
                    .into(thumb);

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlayUtil.runService(mContext, new MusicData(data), true);
                }
            });

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Realm realm = Realm.getInstance(mContext);
                    if (realm.where(RM_PlayListData.class).findAll().size() == 0) {
                        DialogUtil.addPlayListDialog(mContext, data, type);
                    } else {
                        DialogUtil.selectPlayListDialog(mContext, data, type);
                    }
                }
            });
        }
        return view;
    }
}